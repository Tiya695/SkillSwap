package com.skillswap.dto;

import com.skillswap.enums.SwapStatus;
import jakarta.validation.constraints.NotNull;

public record SwapStatusRequest(@NotNull(message = "Status is required") SwapStatus status) {
}
