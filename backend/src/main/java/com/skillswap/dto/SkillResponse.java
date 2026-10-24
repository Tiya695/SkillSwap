package com.skillswap.dto;

import com.skillswap.entity.Skill;

public record SkillResponse(Long id, String name, String category, String description) {

    public static SkillResponse from(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getCategory(), s.getDescription());
    }
}
