package com.skillswap.service;

import com.skillswap.exception.BadRequestException;

import java.util.Map;

public final class PhoneUtil {

    private PhoneUtil() {
    }

    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String cleaned = input.replaceAll("[\\s-]", "");
        if (!cleaned.matches("^(\\+91|91|0)?[6-9]\\d{9}$")) {
            throw new BadRequestException("Invalid contact number", Map.of("phone",
                    "Enter a valid 10 digit Indian mobile number, for example 9876543210 or +91 9876543210"));
        }
        return "+91" + cleaned.substring(cleaned.length() - 10);
    }
}
