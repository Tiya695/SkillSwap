package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillRequest(
        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
        @NotBlank(message = "Category is required") @Size(max = 100, message = "Category must be at most 100 characters") String category,
        @Size(max = 500, message = "Description must be at most 500 characters") String description) {
}
