package com.skillswap.service;

import com.skillswap.dto.ReviewRequest;
import com.skillswap.dto.ReviewResponse;
import com.skillswap.entity.Review;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.enums.SwapStatus;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.SwapRequestRepository;
import com.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SwapRequestRepository swapRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, SwapRequestRepository swapRepository,
                         UserRepository userRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.swapRepository = swapRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public ReviewResponse add(String email, Long swapId, ReviewRequest request) {
        User me = userService.getByEmail(email);
        SwapRequest swap = swapRepository.findById(swapId)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));

        boolean isRequester = swap.getRequester().getId().equals(me.getId());
        boolean isReceiver = swap.getReceiver().getId().equals(me.getId());
        if (!isRequester && !isReceiver) {
            throw new ForbiddenException("You are not part of this swap");
        }
        if (swap.getStatus() != SwapStatus.COMPLETED) {
            throw new ConflictException("You can review a swap only after it is completed");
        }
        if (reviewRepository.existsBySwapRequestIdAndReviewerId(swapId, me.getId())) {
            throw new ConflictException("You have already reviewed this swap");
        }

        Review review = new Review();
        review.setSwapRequest(swap);
        review.setReviewer(me);
        review.setReviewee(isRequester ? swap.getReceiver() : swap.getRequester());
        review.setRating(request.rating());
        review.setComment(request.comment() == null || request.comment().isBlank() ? null : request.comment().trim());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listForUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponse::from).toList();
    }
}
