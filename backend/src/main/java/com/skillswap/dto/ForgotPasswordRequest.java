package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = Rules.GMAIL, flags = Pattern.Flag.CASE_INSENSITIVE, message = Rules.GMAIL_MESSAGE) String email,
        @NotNull(message = "Date of birth is required") LocalDate dateOfBirth,
        @NotBlank(message = "New password is required")
        @Pattern(regexp = Rules.PASSWORD, message = Rules.PASSWORD_MESSAGE) String newPassword) {
}
