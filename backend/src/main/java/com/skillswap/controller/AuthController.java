package com.skillswap.controller;

import com.skillswap.dto.AuthResponse;
import com.skillswap.dto.ForgotPasswordRequest;
import com.skillswap.dto.LoginRequest;
import com.skillswap.dto.MessageResponse;
import com.skillswap.dto.RegisterRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Register and log in (public)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new account")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "400", description = "Validation error or email already registered")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Set a new password using the email and the date of birth given at registration")
    @ApiResponse(responseCode = "200", description = "Password updated")
    @ApiResponse(responseCode = "400", description = "Validation error, or email and date of birth do not match")
    @ApiResponse(responseCode = "429", description = "Too many wrong attempts")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT")
    @ApiResponse(responseCode = "200", description = "Logged in")
    @ApiResponse(responseCode = "401", description = "Wrong email or password")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
