package com.skillswap.dto;

import com.skillswap.enums.MatchType;

import java.util.List;

public record MatchResponse(UserSummary user, MatchType matchType, List<SkillResponse> theyCanTeachYou,
                            List<SkillResponse> youCanTeachThem, int score) {
}
