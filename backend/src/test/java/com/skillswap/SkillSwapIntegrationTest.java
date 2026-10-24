package com.skillswap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.entity.Skill;
import com.skillswap.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SkillSwapIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    SkillRepository skillRepository;

    static final String PASSWORD = "Secret1!";
    static final String DOB = "2000-05-17";

    @Test
    void fullSwapFlow() throws Exception {
        Skill python = skillRepository.save(new Skill("Python", "Programming", null));
        Skill guitar = skillRepository.save(new Skill("Guitar", "Music", null));

        register("Tiya", "tiya@gmail.com");
        register("Rahul", "rahul@gmail.com");
        JsonNode tiyaLogin = login("tiya@gmail.com");
        JsonNode rahulLogin = login("rahul@gmail.com");
        String tiyaToken = tiyaLogin.get("token").asText();
        String rahulToken = rahulLogin.get("token").asText();
        long rahulId = rahulLogin.get("user").get("id").asLong();
        assertThat(tiyaLogin.get("user").has("password")).isFalse();

        addSkill(tiyaToken, python.getId(), "OFFERED");
        addSkill(tiyaToken, guitar.getId(), "WANTED");
        addSkill(rahulToken, guitar.getId(), "OFFERED");
        addSkill(rahulToken, python.getId(), "WANTED");

        mockMvc.perform(authed(get("/api/matches"), tiyaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchType").value("MUTUAL"))
                .andExpect(jsonPath("$[0].user.id").value(rahulId))
                .andExpect(jsonPath("$[0].theyCanTeachYou[0].name").value("Guitar"));

        String swapJson = objectMapper.writeValueAsString(Map.of(
                "receiverId", rahulId, "skillOfferedId", python.getId(), "skillWantedId", guitar.getId(),
                "durationDays", 7, "message", "Let's swap"));
        String created = mockMvc.perform(authed(post("/api/swaps"), tiyaToken)
                        .contentType(MediaType.APPLICATION_JSON).content(swapJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long swapId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(authed(post("/api/swaps"), tiyaToken).contentType(MediaType.APPLICATION_JSON).content(swapJson))
                .andExpect(status().isConflict());

        mockMvc.perform(authed(get("/api/swaps?box=incoming"), rahulToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(authed(get("/api/swaps/" + swapId + "/messages"), tiyaToken)).andExpect(status().isConflict());

        setStatus(rahulToken, swapId, "ACCEPTED").andExpect(status().isOk())
                .andExpect(jsonPath("$.durationDays").value(7))
                .andExpect(jsonPath("$.meetingUrl").value(org.hamcrest.Matchers.startsWith("https://meet.jit.si/SkillSwap-")))
                .andExpect(jsonPath("$.assessmentUnlocked").value(false));

        postJson(tiyaToken, "/api/swaps/" + swapId + "/messages", "{\"text\":\"Hi Rahul\"}").andExpect(status().isCreated());
        mockMvc.perform(authed(get("/api/swaps/" + swapId + "/messages?afterId=0"), rahulToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Hi Rahul"))
                .andExpect(jsonPath("$[0].senderName").value("Tiya"));
        register("Stranger", "stranger@gmail.com");
        String strangerToken = login("stranger@gmail.com").get("token").asText();
        mockMvc.perform(authed(get("/api/swaps/" + swapId + "/messages"), strangerToken)).andExpect(status().isForbidden());

        postJson(tiyaToken, "/api/swaps/" + swapId + "/assessments", QUIZ).andExpect(status().isConflict());
        setStatus(tiyaToken, swapId, "COMPLETED").andExpect(status().isConflict());

        postJson(tiyaToken, "/api/swaps/" + swapId + "/finish-early", null).andExpect(status().isOk());
        postJson(rahulToken, "/api/swaps/" + swapId + "/finish-early", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.assessmentUnlocked").value(true));

        JsonNode tiyaQuiz = json(postJson(tiyaToken, "/api/swaps/" + swapId + "/assessments", QUIZ).andExpect(status().isCreated()));
        JsonNode rahulQuiz = json(postJson(rahulToken, "/api/swaps/" + swapId + "/assessments", QUIZ).andExpect(status().isCreated()));
        long tiyaQuizId = tiyaQuiz.get("id").asLong();
        long rahulQuizId = rahulQuiz.get("id").asLong();
        assertThat(tiyaQuiz.get("skill").get("name").asText()).isEqualTo("Python");
        assertThat(rahulQuiz.get("skill").get("name").asText()).isEqualTo("Guitar");

        JsonNode rahulSees = json(mockMvc.perform(authed(get("/api/swaps/" + swapId + "/assessments"), rahulToken)).andExpect(status().isOk()));
        assertThat(rahulSees.get("toTake").get("questions").get(0).get("correctOption").isNull()).isTrue();
        assertThat(rahulSees.get("toTake").get("questions").get(0).get("options")).hasSize(4);

        assertThat(tiyaQuiz.get("questions").get(0).get("correctOption").asInt()).isEqualTo(1);

        takeTest(rahulToken, tiyaQuizId, tiyaQuiz, 0, 0)
                .andExpect(jsonPath("$.scorePercent").value(0)).andExpect(jsonPath("$.passed").value(false))
                .andExpect(jsonPath("$.status").value("GRADED"))
                .andExpect(jsonPath("$.questions[0].correctOption").value(1));

        takeTest(rahulToken, tiyaQuizId, tiyaQuiz, 1, 2, status().isConflict());
        takeTest(tiyaToken, tiyaQuizId, tiyaQuiz, 1, 2, status().isForbidden());

        postJson(rahulToken, "/api/assessments/" + tiyaQuizId + "/retake", null).andExpect(status().isForbidden());
        postJson(tiyaToken, "/api/assessments/" + tiyaQuizId + "/retake", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt").value(2)).andExpect(jsonPath("$.status").value("OPEN"));
        mockMvc.perform(authed(get("/api/swaps/" + swapId), tiyaToken)).andExpect(jsonPath("$.status").value("ACCEPTED"));

        takeTest(rahulToken, tiyaQuizId, tiyaQuiz, 1, 0)
                .andExpect(jsonPath("$.scorePercent").value(50)).andExpect(jsonPath("$.passed").value(true));
        mockMvc.perform(authed(get("/api/swaps/" + swapId), tiyaToken)).andExpect(jsonPath("$.status").value("ACCEPTED"));

        takeTest(tiyaToken, rahulQuizId, rahulQuiz, 1, 2)
                .andExpect(jsonPath("$.scorePercent").value(100));

        mockMvc.perform(authed(get("/api/swaps/" + swapId), tiyaToken))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(authed(post("/api/swaps/" + swapId + "/reviews"), tiyaToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5,\"comment\":\"Great\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(authed(get("/api/users/" + rahulId), tiyaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgRating").value(5.0))
                .andExpect(jsonPath("$.reviewCount").value(1));
    }

    @Test
    void forgotPasswordResetsThePasswordAndOnlyTheNewOneWorks() throws Exception {
        register("Meera", "meera@gmail.com");

        forgot("meera@gmail.com", "1999-01-01", "NewPass1!").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email and date of birth do not match our records"));

        loginRaw("meera@gmail.com", PASSWORD).andExpect(status().isOk());

        forgot("meera@gmail.com", DOB, "NewPass1!").andExpect(status().isOk());

        loginRaw("meera@gmail.com", PASSWORD).andExpect(status().isUnauthorized());
        loginRaw("meera@gmail.com", "NewPass1!").andExpect(status().isOk());
    }

    @Test
    void profileSavesAnIndianNumberAndRejectsBadOnes() throws Exception {
        register("Dev", "dev@gmail.com");
        String token = login("dev@gmail.com").get("token").asText();

        mockMvc.perform(authed(put("/api/users/me"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev\",\"dateOfBirth\":\"2000-05-17\",\"bio\":\"Hi\",\"phone\":\"+91 98765 43210\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+919876543210"));
        mockMvc.perform(authed(get("/api/users/me"), token))
                .andExpect(jsonPath("$.phone").value("+919876543210"))
                .andExpect(jsonPath("$.dateOfBirth").value(DOB));

        mockMvc.perform(authed(put("/api/users/me"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dev\",\"dateOfBirth\":\"2000-05-17\",\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());
    }

    @Test
    void profilePhotoCanBeUploadedServedAndRemoved() throws Exception {
        register("Isha", "isha@gmail.com");
        String token = login("isha@gmail.com").get("token").asText();
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

        String body = mockMvc.perform(authed(multipart("/api/users/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", png)), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String url = objectMapper.readTree(body).get("avatarUrl").asText();
        assertThat(url).startsWith("/api/files/avatars/").endsWith(".png");

        mockMvc.perform(get(url)).andExpect(status().isOk()).andExpect(header().string("Content-Type", "image/png"));

        mockMvc.perform(authed(multipart("/api/users/me/avatar")
                        .file(new MockMultipartFile("file", "x.png", "image/png", "hello".getBytes())), token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authed(delete("/api/users/me/avatar"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
        mockMvc.perform(get(url)).andExpect(status().isNotFound());
    }

    @Test
    void anyUserCanAddASkillButNotEditOrDeleteIt() throws Exception {
        register("Kabir", "kabir@gmail.com");
        String token = login("kabir@gmail.com").get("token").asText();

        String created = mockMvc.perform(authed(post("/api/skills"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chess\",\"category\":\"Games\",\"description\":\"Openings\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(authed(post("/api/skills"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"chess\",\"category\":\"Games\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(authed(delete("/api/skills/" + id), token)).andExpect(status().isForbidden());

        String added = mockMvc.perform(authed(post("/api/me/skills"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":" + id + ",\"type\":\"OFFERED\",\"level\":\"BEGINNER\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long mine = objectMapper.readTree(added).get("id").asLong();
        mockMvc.perform(authed(delete("/api/me/skills/" + mine), token)).andExpect(status().isNoContent());
        mockMvc.perform(authed(get("/api/me/skills"), token)).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void changedDateOfBirthIsUsedForTheNextPasswordReset() throws Exception {
        register("Sana", "sana@gmail.com");
        String token = login("sana@gmail.com").get("token").asText();

        mockMvc.perform(authed(put("/api/users/me"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sana\",\"dateOfBirth\":\"1998-12-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfBirth").value("1998-12-31"));

        mockMvc.perform(authed(put("/api/users/me"), token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sana\",\"dateOfBirth\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());

        forgot("sana@gmail.com", DOB, "NewPass1!").andExpect(status().isBadRequest());
        forgot("sana@gmail.com", "1998-12-31", "NewPass1!").andExpect(status().isOk());
        loginRaw("sana@gmail.com", "NewPass1!").andExpect(status().isOk());
    }

    @Test
    void deletingAnAccountRemovesItsDataAndNothingElse() throws Exception {
        Skill python = skillRepository.save(new Skill("Python D", "Programming", null));
        Skill guitar = skillRepository.save(new Skill("Guitar D", "Music", null));
        register("Delta", "delta@gmail.com");
        register("Echo", "echo@gmail.com");
        JsonNode delta = login("delta@gmail.com");
        JsonNode echo = login("echo@gmail.com");
        String deltaToken = delta.get("token").asText();
        String echoToken = echo.get("token").asText();
        long echoId = echo.get("user").get("id").asLong();

        addSkill(deltaToken, python.getId(), "OFFERED");
        addSkill(echoToken, guitar.getId(), "OFFERED");
        String swapJson = objectMapper.writeValueAsString(Map.of("receiverId", echoId, "skillOfferedId", python.getId(),
                "skillWantedId", guitar.getId(), "durationDays", 3));
        long swapId = json(postJson(deltaToken, "/api/swaps", swapJson).andExpect(status().isCreated())).get("id").asLong();
        setStatus(echoToken, swapId, "ACCEPTED").andExpect(status().isOk());
        postJson(deltaToken, "/api/swaps/" + swapId + "/messages", "{\"text\":\"hi\"}").andExpect(status().isCreated());

        mockMvc.perform(authed(delete("/api/users/me"), deltaToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Wrong1!x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
        loginRaw("delta@gmail.com", PASSWORD).andExpect(status().isOk());

        mockMvc.perform(authed(delete("/api/users/me"), deltaToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        loginRaw("delta@gmail.com", PASSWORD).andExpect(status().isUnauthorized());

        mockMvc.perform(authed(get("/api/users/me"), deltaToken)).andExpect(status().isUnauthorized());

        mockMvc.perform(authed(get("/api/users/me"), echoToken)).andExpect(status().isOk());
        mockMvc.perform(authed(get("/api/swaps?box=incoming"), echoToken))
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(authed(get("/api/swaps/" + swapId), echoToken)).andExpect(status().isNotFound());

        register("Delta Again", "delta@gmail.com");
    }

    @Test
    void questionDraftsAreLockedAndReportAMissingModelClearly() throws Exception {
        Skill python = skillRepository.save(new Skill("Python G", "Programming", null));
        Skill guitar = skillRepository.save(new Skill("Guitar G", "Music", null));
        register("Gita", "gita@gmail.com");
        register("Hari", "hari@gmail.com");
        String gitaToken = login("gita@gmail.com").get("token").asText();
        JsonNode hari = login("hari@gmail.com");
        String hariToken = hari.get("token").asText();
        addSkill(gitaToken, python.getId(), "OFFERED");
        addSkill(hariToken, guitar.getId(), "OFFERED");
        String swapJson = objectMapper.writeValueAsString(Map.of("receiverId", hari.get("user").get("id").asLong(),
                "skillOfferedId", python.getId(), "skillWantedId", guitar.getId(), "durationDays", 5));
        long swapId = json(postJson(gitaToken, "/api/swaps", swapJson).andExpect(status().isCreated())).get("id").asLong();
        setStatus(hariToken, swapId, "ACCEPTED").andExpect(status().isOk());

        String body = "{\"topic\":\"Lists and loops\",\"count\":5}";

        postJson(gitaToken, "/api/swaps/" + swapId + "/assessments/generate", body).andExpect(status().isConflict());

        postJson(gitaToken, "/api/swaps/" + swapId + "/finish-early", null).andExpect(status().isOk());
        postJson(hariToken, "/api/swaps/" + swapId + "/finish-early", null).andExpect(status().isOk());

        postJson(gitaToken, "/api/swaps/" + swapId + "/assessments/generate", "{\"topic\":\"\"}").andExpect(status().isBadRequest());
        postJson(gitaToken, "/api/swaps/" + swapId + "/assessments/generate", "{\"topic\":\"x\",\"count\":11}").andExpect(status().isBadRequest());

        postJson(gitaToken, "/api/swaps/" + swapId + "/assessments/generate", body).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ollama")));
    }

    private static final String QUIZ = "{\"questions\":["
            + "{\"text\":\"What is a list?\",\"maxPoints\":10,\"options\":[\"A number\",\"An ordered collection\",\"A file\",\"A loop\"],\"correctOption\":1},"
            + "{\"text\":\"Which keyword starts a loop?\",\"maxPoints\":10,\"options\":[\"if\",\"def\",\"for\",\"class\"],\"correctOption\":2}]}";

    private ResultActions postJson(String token, String path, String body) throws Exception {
        var builder = authed(post(path), token).contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(body == null ? builder : builder.content(body));
    }

    private JsonNode json(ResultActions actions) throws Exception {
        return objectMapper.readTree(actions.andReturn().getResponse().getContentAsString());
    }

    private ResultActions takeTest(String learnerToken, long quizId, JsonNode quiz, int pick1, int pick2) throws Exception {
        return takeTest(learnerToken, quizId, quiz, pick1, pick2, status().isOk());
    }

    private ResultActions takeTest(String learnerToken, long quizId, JsonNode quiz, int pick1, int pick2,
                                   org.springframework.test.web.servlet.ResultMatcher expected) throws Exception {
        JsonNode questions = quiz.get("questions");
        String answers = "{\"answers\":[{\"questionId\":" + questions.get(0).get("id").asLong() + ",\"selectedOption\":" + pick1 + "},"
                + "{\"questionId\":" + questions.get(1).get("id").asLong() + ",\"selectedOption\":" + pick2 + "}]}";
        return postJson(learnerToken, "/api/assessments/" + quizId + "/submit", answers).andExpect(expected);
    }

    private void register(String name, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "email", email, "dateOfBirth", DOB, "password", PASSWORD))))
                .andExpect(status().isCreated());
    }

    private JsonNode login(String email) throws Exception {
        String body = loginRaw(email, PASSWORD).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private ResultActions loginRaw(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))));
    }

    private ResultActions forgot(String email, String dob, String newPassword) throws Exception {
        return mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "dateOfBirth", dob, "newPassword", newPassword))));
    }

    private void addSkill(String token, Long skillId, String type) throws Exception {
        mockMvc.perform(authed(post("/api/me/skills"), token).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("skillId", skillId, "type", type, "level", "BEGINNER"))))
                .andExpect(status().isCreated());
    }

    private ResultActions setStatus(String token, long swapId, String status)
            throws Exception {
        return mockMvc.perform(authed(patch("/api/swaps/" + swapId + "/status"), token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + status + "\"}"));
    }

    private MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }
}
