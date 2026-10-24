package com.skillswap.dto;

import com.skillswap.entity.SwapMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(Long id, Long senderId, String senderName, String text, LocalDateTime createdAt) {

    public static ChatMessageResponse from(SwapMessage m) {
        return new ChatMessageResponse(m.getId(), m.getSender().getId(), m.getSender().getName(), m.getText(),
                m.getCreatedAt());
    }
}
