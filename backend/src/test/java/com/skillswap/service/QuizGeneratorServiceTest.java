package com.skillswap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.exception.ServiceUnavailableException;
import com.skillswap.exception.TooManyRequestsException;
import com.skillswap.service.QuizGeneratorService.GeneratedQuestion;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuizGeneratorServiceTest {

    static final String ONE_QUESTION =
            "{\"questions\":[{\"question\":\"What is a Python list?\",\"options\":[\"A number\",\"An ordered collection\",\"A file\",\"A loop\"],\"answerIndex\":1}]}";

    HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private QuizGeneratorService serviceFor(int port, long timeoutSeconds) {
        return new QuizGeneratorService(new ObjectMapper(), "http://localhost:" + port + "/", "phi3:latest", timeoutSeconds);
    }

    private int startFakeOllama(int status, String body, CountDownLatch... gate) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/generate", exchange -> {
            try {
                for (CountDownLatch g : gate) {
                    g.await(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server.getAddress().getPort();
    }

    private static String ollamaReply(String innerJson) {
        return new ObjectMapper().createObjectNode().put("response", innerJson).toString();
    }

    @Test
    void returnsMultipleChoiceQuestionsFromTheModel() throws Exception {
        int port = startFakeOllama(200, ollamaReply(ONE_QUESTION));

        List<GeneratedQuestion> questions = serviceFor(port, 5).generate("Python", "Lists", 10);

        assertThat(questions).hasSize(1);
        GeneratedQuestion q = questions.get(0);
        assertThat(q.text()).isEqualTo("What is a Python list?");
        assertThat(q.options()).containsExactlyInAnyOrder("A number", "An ordered collection", "A file", "A loop");

        assertThat(q.options().get(q.correctOption())).isEqualTo("An ordered collection");
    }

    @Test
    void optionsAreShuffledSoTheCorrectAnswerIsNotAlwaysFirst() {
        QuizGeneratorService service = serviceFor(9, 5);
        Set<Integer> positions = new HashSet<>();

        for (int i = 0; i < 60; i++) {
            GeneratedQuestion q = service.parseQuestions(ONE_QUESTION, 1).get(0);
            assertThat(q.options().get(q.correctOption())).isEqualTo("An ordered collection");
            positions.add(q.correctOption());
        }

        assertThat(positions.size()).isGreaterThan(1);
    }

    @Test
    void lettersAndNumbersInFrontOfTheOptionsAreRemoved() {
        String json = "{\"questions\":[{\"question\":\"1. Which keyword starts a loop?\","
                + "\"options\":[\"a) if\",\"b) def\",\"c) for\",\"d) class\"],\"answerIndex\":2}]}";

        GeneratedQuestion q = serviceFor(9, 5).parseQuestions(json, 5).get(0);

        assertThat(q.text()).isEqualTo("Which keyword starts a loop?");
        assertThat(q.options()).containsExactlyInAnyOrder("if", "def", "for", "class");
        assertThat(q.options().get(q.correctOption())).isEqualTo("for");
    }

    @Test
    void theCorrectAnswerCanBeGivenAsTextOrLetter() {
        String byText = "{\"questions\":[{\"question\":\"Pick the loop keyword\",\"options\":[\"if\",\"for\",\"def\"],\"answer\":\"For\"}]}";
        String byLetter = "{\"questions\":[{\"question\":\"Pick the loop keyword\",\"options\":[\"if\",\"for\",\"def\"],\"answer\":\"b\"}]}";

        for (String json : List.of(byText, byLetter)) {
            GeneratedQuestion q = serviceFor(9, 5).parseQuestions(json, 5).get(0);
            assertThat(q.options().get(q.correctOption())).isEqualTo("for");
        }
    }

    @Test
    void questionsWithoutAClearCorrectAnswerOrEnoughOptionsAreDropped() {
        String json = "{\"questions\":["
                + "{\"question\":\"No answer given here\",\"options\":[\"a\",\"b\",\"c\"]},"
                + "{\"question\":\"Only one option here\",\"options\":[\"a\"],\"answerIndex\":0},"
                + "{\"question\":\"Index is out of range\",\"options\":[\"a\",\"b\"],\"answerIndex\":7},"
                + "{\"question\":\"A good question here\",\"options\":[\"x\",\"y\"],\"answerIndex\":1}]}";

        List<GeneratedQuestion> result = serviceFor(9, 5).parseQuestions(json, 10);

        assertThat(result).extracting(GeneratedQuestion::text).containsExactly("A good question here");
    }

    @Test
    void duplicatesAreDroppedAndTheCountIsRespected() {
        String q = "{\"question\":\"What is a loop used for?\",\"options\":[\"repeat\",\"print\"],\"answerIndex\":0}";
        String q2 = "{\"question\":\"What is a list used for?\",\"options\":[\"store\",\"print\"],\"answerIndex\":0}";
        String q3 = "{\"question\":\"What is a dict used for?\",\"options\":[\"map\",\"print\"],\"answerIndex\":0}";

        List<GeneratedQuestion> result = serviceFor(9, 5).parseQuestions("{\"questions\":[" + q + "," + q + "," + q2 + "," + q3 + "]}", 2);

        assertThat(result).extracting(GeneratedQuestion::text).containsExactly("What is a loop used for?", "What is a list used for?");
    }

    @Test
    void unusableAnswerGivesAClearError() {
        QuizGeneratorService service = serviceFor(9, 5);

        assertThatThrownBy(() -> service.parseQuestions("not json at all", 10)).isInstanceOf(ServiceUnavailableException.class);
        assertThatThrownBy(() -> service.parseQuestions("{\"questions\":[]}", 10))
                .isInstanceOf(ServiceUnavailableException.class).hasMessageContaining("usable questions");

        assertThatThrownBy(() -> service.parseQuestions("{\"questions\":[\"What is a list?\"]}", 10))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void ollamaNotRunningGivesAHelpfulMessage() {

        assertThatThrownBy(() -> serviceFor(9, 5).generate("Python", "Lists", 5))
                .isInstanceOf(ServiceUnavailableException.class).hasMessageContaining("Ollama");
    }

    @Test
    void missingModelTellsHowToInstallIt() throws Exception {
        int port = startFakeOllama(404, "{\"error\":\"model not found\"}");

        assertThatThrownBy(() -> serviceFor(port, 5).generate("Python", "Lists", 5))
                .isInstanceOf(ServiceUnavailableException.class).hasMessageContaining("ollama pull phi3:latest");
    }

    @Test
    void slowModelIsStoppedAfterTheTimeout() throws Exception {
        int port = startFakeOllama(200, ollamaReply(ONE_QUESTION), new CountDownLatch(1));

        assertThatThrownBy(() -> serviceFor(port, 1).generate("Python", "Lists", 5))
                .isInstanceOf(ServiceUnavailableException.class).hasMessageContaining("too long");
    }

    @Test
    void onlyOneQuestionSetIsWrittenAtATime() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        int port = startFakeOllama(200, ollamaReply(ONE_QUESTION), release);
        QuizGeneratorService service = serviceFor(port, 10);

        Thread first = new Thread(() -> service.generate("Python", "Lists", 1));
        first.start();
        Thread.sleep(500);

        assertThatThrownBy(() -> service.generate("Python", "Loops", 1)).isInstanceOf(TooManyRequestsException.class);

        release.countDown();
        first.join(5000);
    }
}
