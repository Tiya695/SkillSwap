package com.skillswap.dto;

import com.skillswap.entity.User;
import com.skillswap.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(Long id, String name, String email, String bio, String avatarUrl, String phone,
                           LocalDate dateOfBirth, Role role, LocalDateTime createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getBio(), u.getAvatarUrl(), u.getPhone(),
                u.getDateOfBirth(), u.getRole(), u.getCreatedAt());
    }
}
