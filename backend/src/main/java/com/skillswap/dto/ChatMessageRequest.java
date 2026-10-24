package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank(message = "Write a message") @Size(max = 1000, message = "Message must be at most 1000 characters") String text) {
}
