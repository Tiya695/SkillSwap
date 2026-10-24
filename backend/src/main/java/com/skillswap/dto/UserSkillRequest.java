package com.skillswap.dto;

import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserSkillRequest(
        @NotNull(message = "Skill is required") Long skillId,
        @NotNull(message = "Type is required") SkillType type,
        @NotNull(message = "Level is required") SkillLevel level,
        @Size(max = 300, message = "Notes must be at most 300 characters") String notes) {
}
