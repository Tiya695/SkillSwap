package com.skillswap.dto;

import com.skillswap.entity.User;

public record UserSummary(Long id, String name, String avatarUrl, String bio) {

    public static UserSummary from(User u) {
        return new UserSummary(u.getId(), u.getName(), u.getAvatarUrl(), u.getBio());
    }
}
