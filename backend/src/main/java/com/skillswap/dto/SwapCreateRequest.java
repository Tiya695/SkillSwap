package com.skillswap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SwapCreateRequest(
        @NotNull(message = "Receiver is required") Long receiverId,
        @NotNull(message = "Choose the skill you will teach") Long skillOfferedId,
        @NotNull(message = "Choose the skill you want to learn") Long skillWantedId,
        @NotNull(message = "Enter how many days you will teach each other")
        @Min(value = 1, message = "Duration must be at least 1 day")
        @Max(value = 365, message = "Duration can be at most 365 days") Integer durationDays,
        @Size(max = 500, message = "Message must be at most 500 characters") String message) {
}
