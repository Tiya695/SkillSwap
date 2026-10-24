package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
        @NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,
        @Size(max = 500, message = "Bio must be at most 500 characters") String bio,
        @Size(max = 20, message = "Contact number is too long") String phone) {
}
