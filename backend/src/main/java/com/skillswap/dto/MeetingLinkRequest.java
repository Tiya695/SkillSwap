package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MeetingLinkRequest(
        @NotBlank(message = "Paste a link")
        @Size(max = 500, message = "The link is too long")
        @Pattern(regexp = "^https://[^\\s]+$", message = "The link must start with https://") String url) {
}
