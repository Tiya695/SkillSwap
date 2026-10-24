package com.skillswap.dto;

import com.skillswap.entity.Review;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long swapId, UserSummary reviewer, UserSummary reviewee, int rating,
                             String comment, LocalDateTime createdAt) {

    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId(), r.getSwapRequest().getId(), UserSummary.from(r.getReviewer()),
                UserSummary.from(r.getReviewee()), r.getRating(), r.getComment(), r.getCreatedAt());
    }
}
