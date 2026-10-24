package com.skillswap.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PublicProfileResponse(UserSummary user, LocalDateTime memberSince, List<UserSkillResponse> offered,
                                    List<UserSkillResponse> wanted, Double avgRating, long reviewCount) {
}
