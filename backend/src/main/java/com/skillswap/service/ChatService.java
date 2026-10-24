package com.skillswap.service;

import com.skillswap.dto.ChatMessageRequest;
import com.skillswap.dto.ChatMessageResponse;
import com.skillswap.entity.SwapMessage;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.enums.SwapStatus;
import com.skillswap.exception.ConflictException;
import com.skillswap.repository.SwapMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final SwapMessageRepository messageRepository;
    private final SwapAccessService access;

    public ChatService(SwapMessageRepository messageRepository, SwapAccessService access) {
        this.messageRepository = messageRepository;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> list(String email, Long swapId, Long afterId) {
        SwapRequest swap = access.requireParticipant(access.me(email), swapId);
        requireChatOpen(swap, false);
        return messageRepository
                .findTop200BySwapRequestIdAndIdGreaterThanOrderByIdAsc(swapId, afterId == null ? 0L : afterId)
                .stream().map(ChatMessageResponse::from).toList();
    }

    @Transactional
    public ChatMessageResponse send(String email, Long swapId, ChatMessageRequest request) {
        User me = access.me(email);
        SwapRequest swap = access.requireParticipant(me, swapId);
        requireChatOpen(swap, true);

        SwapMessage message = new SwapMessage();
        message.setSwapRequest(swap);
        message.setSender(me);
        message.setText(request.text().trim());
        return ChatMessageResponse.from(messageRepository.save(message));
    }

    private void requireChatOpen(SwapRequest swap, boolean forSending) {
        boolean ok = swap.getStatus() == SwapStatus.ACCEPTED || (!forSending && swap.getStatus() == SwapStatus.COMPLETED);
        if (!ok) {
            throw new ConflictException(forSending
                    ? "Chat is available while the swap is accepted"
                    : "Chat opens after the request is accepted");
        }
    }
}
