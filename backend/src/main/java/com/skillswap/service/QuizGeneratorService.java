package com.skillswap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.exception.ServiceUnavailableException;
import com.skillswap.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

@Service
public class QuizGeneratorService {

    public record GeneratedQuestion(String text, List<String> options, int correctOption) {
    }

    private static final Logger log = LoggerFactory.getLogger(QuizGeneratorService.class);
    private static final int MAX_QUESTION_LENGTH = 500;
    private static final int MAX_OPTION_LENGTH = 200;
    private static final int MAX_OPTIONS = 6;

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;
    private final Duration timeout;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final Semaphore busy = new Semaphore(1);

    public QuizGeneratorService(ObjectMapper objectMapper,
                                @Value("${app.quiz.ollama-url}") String baseUrl,
                                @Value("${app.quiz.model}") String model,
                                @Value("${app.quiz.timeout-seconds}") long timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    public List<GeneratedQuestion> generate(String skillName, String topic, int count) {
        if (!busy.tryAcquire()) {
            throw new TooManyRequestsException("Questions are already being written. Please try again in a moment.");
        }
        try {
            String prompt = "You are helping a teacher write a final multiple choice test for a learner. "
                    + "The skill taught is \"" + skillName + "\" and the topic of the test is \"" + topic + "\". "
                    + "Write exactly " + count + " different multiple choice questions about that topic. "
                    + "Each question has exactly 4 answer options and exactly one correct option. "
                    + "The wrong options must be believable but clearly wrong to someone who learned the topic. "
                    + "Do not number the questions and do not put letters or numbers in front of the options. "
                    + "Reply only with JSON in this shape: {\"questions\":[{\"question\":\"...\","
                    + "\"options\":[\"option 1\",\"option 2\",\"option 3\",\"option 4\"],\"answerIndex\":0}]} "
                    + "where answerIndex is the position (0 to 3) of the correct option.";
            return parseQuestions(callOllama(prompt), count);
        } finally {
            busy.release();
        }
    }

    private String callOllama(String prompt) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "format", "json",
                    "keep_alive", "10m",
                    "options", Map.of("temperature", 0.4, "num_predict", 3000)));
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/generate"))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new ServiceUnavailableException("The model \"" + model + "\" is not installed in Ollama. "
                        + "Install it with: ollama pull " + model);
            }
            if (response.statusCode() != 200) {
                log.warn("Ollama answered {}: {}", response.statusCode(), response.body());
                throw new ServiceUnavailableException("The question writer returned an error. Please try again or write the questions yourself.");
            }
            return objectMapper.readTree(response.body()).path("response").asText("");
        } catch (ConnectException e) {
            throw new ServiceUnavailableException(
                    "The question writer (Ollama) is not running. Start Ollama, or write the questions yourself.");
        } catch (HttpTimeoutException e) {
            throw new ServiceUnavailableException(
                    "The question writer took too long. Try fewer questions or a shorter topic, or write them yourself.");
        } catch (IOException e) {
            log.warn("Could not reach Ollama: {}", e.toString());
            throw new ServiceUnavailableException(
                    "The question writer (Ollama) could not be reached. Start Ollama, or write the questions yourself.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceUnavailableException("The request was interrupted. Please try again.");
        }
    }

    List<GeneratedQuestion> parseQuestions(String json, int count) {
        JsonNode array;
        try {
            JsonNode root = objectMapper.readTree(json);
            array = root.isArray() ? root : root.path("questions");
        } catch (IOException e) {
            throw new ServiceUnavailableException("The question writer sent an answer that could not be read. Please try again.");
        }

        Set<String> seen = new LinkedHashSet<>();
        List<GeneratedQuestion> result = new ArrayList<>();
        for (JsonNode item : array) {
            if (!item.isObject()) {
                continue;
            }
            String text = clean(item.path("question").asText(item.path("text").asText("")), MAX_QUESTION_LENGTH)
                    .replaceFirst("^(?:Q\\s*)?\\d+\\s*[.):-]\\s*", "");
            List<String> options = cleanOptions(item.path("options"));
            int correct = findCorrect(item, options);
            if (text.length() < 8 || options.size() < 2 || correct < 0 || !seen.add(text.toLowerCase(Locale.ROOT))) {
                continue;
            }
            result.add(shuffled(text, options, correct));
            if (result.size() == count) {
                break;
            }
        }
        if (result.isEmpty()) {
            throw new ServiceUnavailableException("The question writer did not produce usable questions. Try a clearer topic.");
        }
        return result;
    }

    private List<String> cleanOptions(JsonNode node) {
        Set<String> options = new LinkedHashSet<>();
        for (JsonNode o : node) {
            String raw = o.isTextual() ? o.asText() : o.path("text").asText("");
            String text = clean(raw, MAX_OPTION_LENGTH).replaceFirst("^(?:[A-Fa-f]|\\d)\\s*[.):-]\\s+", "");
            if (!text.isBlank()) {
                options.add(text);
            }
            if (options.size() == MAX_OPTIONS) {
                break;
            }
        }
        return new ArrayList<>(options);
    }

    private int findCorrect(JsonNode item, List<String> options) {
        JsonNode index = item.path("answerIndex");
        if (index.isIntegralNumber() && index.asInt() >= 0 && index.asInt() < options.size()) {
            return index.asInt();
        }
        for (String field : new String[]{"answer", "correctAnswer", "correct"}) {
            JsonNode node = item.path(field);
            if (!node.isTextual()) {
                continue;
            }
            String wanted = clean(node.asText(), MAX_OPTION_LENGTH).replaceFirst("^(?:[A-Fa-f]|\\d)\\s*[.):-]\\s+", "");
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equalsIgnoreCase(wanted)) {
                    return i;
                }
            }
            if (wanted.matches("(?i)[a-f]") && wanted.toLowerCase(Locale.ROOT).charAt(0) - 'a' < options.size()) {
                return wanted.toLowerCase(Locale.ROOT).charAt(0) - 'a';
            }
        }
        return -1;
    }

    private GeneratedQuestion shuffled(String text, List<String> options, int correct) {
        String correctText = options.get(correct);
        List<String> mixed = new ArrayList<>(options);
        Collections.shuffle(mixed);
        return new GeneratedQuestion(text, mixed, mixed.indexOf(correctText));
    }

    private String clean(String value, int maxLength) {
        String text = value.replaceAll("\\s+", " ").trim();
        return text.length() > maxLength ? text.substring(0, maxLength - 1).trim() + "…" : text;
    }
}
