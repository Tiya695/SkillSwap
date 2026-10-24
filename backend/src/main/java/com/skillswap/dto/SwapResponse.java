package com.skillswap.dto;

import com.skillswap.entity.SwapRequest;
import com.skillswap.enums.SwapStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SwapResponse(Long id, UserSummary requester, UserSummary receiver, SkillResponse skillOffered,
                           SkillResponse skillWanted, String message, SwapStatus status, boolean reviewedByMe,
                           int durationDays, LocalDate startDate, LocalDate endDate, String meetingUrl,
                           boolean assessmentUnlocked, boolean finishedEarlyByMe, boolean finishedEarlyByOther,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static SwapResponse from(SwapRequest s, boolean reviewedByMe, Long meId) {
        boolean iAmRequester = s.getRequester().getId().equals(meId);
        return new SwapResponse(s.getId(), UserSummary.from(s.getRequester()), UserSummary.from(s.getReceiver()),
                SkillResponse.from(s.getSkillOffered()), SkillResponse.from(s.getSkillWanted()), s.getMessage(),
                s.getStatus(), reviewedByMe, s.getDurationDays(), s.getStartDate(), s.getEndDate(), s.getMeetingUrl(),
                s.isAssessmentUnlocked(),
                iAmRequester ? s.isRequesterFinishedEarly() : s.isReceiverFinishedEarly(),
                iAmRequester ? s.isReceiverFinishedEarly() : s.isRequesterFinishedEarly(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
