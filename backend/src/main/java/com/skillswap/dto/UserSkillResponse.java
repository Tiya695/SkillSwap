package com.skillswap.dto;

import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;

public record UserSkillResponse(Long id, SkillResponse skill, SkillType type, SkillLevel level, String notes) {

    public static UserSkillResponse from(UserSkill us) {
        return new UserSkillResponse(us.getId(), SkillResponse.from(us.getSkill()), us.getType(), us.getLevel(),
                us.getNotes());
    }
}
