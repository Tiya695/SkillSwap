package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
        @NotBlank(message = "Email is required")
        @Pattern(regexp = Rules.GMAIL, flags = Pattern.Flag.CASE_INSENSITIVE, message = Rules.GMAIL_MESSAGE) String email,
        @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @NotBlank(message = "Password is required")
        @Pattern(regexp = Rules.PASSWORD, message = Rules.PASSWORD_MESSAGE) String password) {
}
