package com.skillswap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.config.CorsConfig;
import com.skillswap.config.SecurityConfig;
import com.skillswap.dto.MessageResponse;
import com.skillswap.dto.Rules;
import com.skillswap.dto.SkillResponse;
import com.skillswap.dto.UserResponse;
import com.skillswap.enums.Role;
import com.skillswap.exception.GlobalExceptionHandler;
import com.skillswap.security.CustomUserDetailsService;
import com.skillswap.security.JwtAuthFilter;
import com.skillswap.security.JwtUtil;
import com.skillswap.service.AuthService;
import com.skillswap.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, SkillController.class, HealthController.class})
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class ControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;
    @MockitoBean
    SkillService skillService;
    @MockitoBean
    JwtUtil jwtUtil;
    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private static final String REGISTER_JSON =
            "{\"name\":\"Tiya\",\"email\":\"tiya@gmail.com\",\"dateOfBirth\":\"2000-01-01\",\"password\":\"Secret1!\"}";

    private static final String SKILL_JSON = "{\"name\":\"Chess\",\"category\":\"Games\",\"description\":\"Openings\"}";

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void registerReturns201() throws Exception {
        when(authService.register(any())).thenReturn(
                new UserResponse(1L, "Tiya", "tiya@gmail.com", null, null, null, LocalDate.of(2000, 1, 1), Role.USER,
                        LocalDateTime.now()));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("tiya@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerValidationErrorsAreReturnedPerField() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"nope\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").value("Date of birth is required"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void registerRejectsNonGmailEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON.replace("tiya@gmail.com", "tiya@yahoo.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value(Rules.GMAIL_MESSAGE));
    }

    @Test
    void registerRejectsWeakPasswords() throws Exception {
        for (String weak : new String[]{"secret1!", "SECRET1!", "Secrets!!", "Secret12", "Se1!"}) {
            mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                            .content(REGISTER_JSON.replace("Secret1!", weak)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.password").exists());
        }
    }

    @Test
    void registerRejectsFutureDateOfBirth() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_JSON.replace("2000-01-01", LocalDate.now().plusDays(1).toString())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
    }

    @Test
    void forgotPasswordIsPublicAndValidated() throws Exception {
        when(authService.resetPassword(any())).thenReturn(new MessageResponse("Password updated."));

        mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tiya@gmail.com\",\"dateOfBirth\":\"2000-01-01\",\"newPassword\":\"NewPass1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated."));

        mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tiya@gmail.com\",\"dateOfBirth\":\"2000-01-01\",\"newPassword\":\"weak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCanAddANewSkill() throws Exception {
        when(skillService.create(any())).thenReturn(new SkillResponse(8L, "Chess", "Games", "Openings"));

        mockMvc.perform(post("/api/skills").contentType(MediaType.APPLICATION_JSON).content(SKILL_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotEditOrDeleteSkills() throws Exception {
        mockMvc.perform(put("/api/skills/1").contentType(MediaType.APPLICATION_JSON).content(SKILL_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(delete("/api/skills/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAddASkill() throws Exception {
        when(skillService.create(any())).thenReturn(new SkillResponse(7L, "Chess", "Games", "Openings"));

        mockMvc.perform(post("/api/skills").contentType(MediaType.APPLICATION_JSON).content(SKILL_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidSkillReturns400() throws Exception {
        mockMvc.perform(post("/api/skills").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"category\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
}
