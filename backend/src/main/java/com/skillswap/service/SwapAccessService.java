package com.skillswap.service;

import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.SwapRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class SwapAccessService {

    private final SwapRequestRepository swapRepository;
    private final UserService userService;

    public SwapAccessService(SwapRequestRepository swapRepository, UserService userService) {
        this.swapRepository = swapRepository;
        this.userService = userService;
    }

    public User me(String email) {
        return userService.getByEmail(email);
    }

    public SwapRequest requireParticipant(User me, Long swapId) {
        SwapRequest swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));
        if (!isParticipant(swap, me)) {
            throw new ForbiddenException("You are not part of this swap");
        }
        return swap;
    }

    public static boolean isParticipant(SwapRequest swap, User me) {
        return swap.getRequester().getId().equals(me.getId()) || swap.getReceiver().getId().equals(me.getId());
    }
}
