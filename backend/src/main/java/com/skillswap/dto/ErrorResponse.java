package com.skillswap.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
                            Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path,
                fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors);
    }
}
