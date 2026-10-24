package com.skillswap.service;

import com.skillswap.dto.PageResponse;
import com.skillswap.dto.SwapCreateRequest;
import com.skillswap.dto.SwapResponse;
import com.skillswap.entity.Skill;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.enums.SkillType;
import com.skillswap.enums.SwapStatus;
import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.SwapRequestRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class SwapService {

    private final SwapRequestRepository swapRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final SkillService skillService;

    public SwapService(SwapRequestRepository swapRepository, UserSkillRepository userSkillRepository,
                       UserRepository userRepository, ReviewRepository reviewRepository,
                       UserService userService, SkillService skillService) {
        this.swapRepository = swapRepository;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.skillService = skillService;
    }

    @Transactional
    public SwapResponse create(String email, SwapCreateRequest request) {
        User me = userService.getByEmail(email);
        if (request.receiverId().equals(me.getId())) {
            throw new BadRequestException("You cannot send a swap request to yourself");
        }
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Skill offered = skillService.find(request.skillOfferedId());
        Skill wanted = skillService.find(request.skillWantedId());

        if (!userSkillRepository.existsByUserIdAndSkillIdAndType(me.getId(), offered.getId(), SkillType.OFFERED)) {
            throw new BadRequestException("You can only offer a skill from your own teaching list",
                    Map.of("skillOfferedId", "Pick a skill you can teach"));
        }
        if (!userSkillRepository.existsByUserIdAndSkillIdAndType(receiver.getId(), wanted.getId(), SkillType.OFFERED)) {
            throw new BadRequestException("This person does not teach that skill",
                    Map.of("skillWantedId", "Pick a skill this person teaches"));
        }
        if (swapRepository.existsByRequesterIdAndReceiverIdAndSkillOfferedIdAndSkillWantedIdAndStatus(
                me.getId(), receiver.getId(), offered.getId(), wanted.getId(), SwapStatus.PENDING)) {
            throw new ConflictException("You already have a pending request for this swap");
        }

        SwapRequest swap = new SwapRequest();
        swap.setRequester(me);
        swap.setReceiver(receiver);
        swap.setSkillOffered(offered);
        swap.setSkillWanted(wanted);
        swap.setMessage(request.message() == null || request.message().isBlank() ? null : request.message().trim());
        swap.setDurationDays(request.durationDays());
        swap.setStatus(SwapStatus.PENDING);
        return SwapResponse.from(swapRepository.save(swap), false, me.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<SwapResponse> list(String email, String box, SwapStatus status, int page, int size) {
        User me = userService.getByEmail(email);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean incoming;
        if (box == null || box.equalsIgnoreCase("incoming")) {
            incoming = true;
        } else if (box.equalsIgnoreCase("outgoing")) {
            incoming = false;
        } else {
            throw new BadRequestException("box must be incoming or outgoing");
        }

        Page<SwapRequest> result;
        if (incoming) {
            result = status == null ? swapRepository.findByReceiverId(me.getId(), pageable)
                    : swapRepository.findByReceiverIdAndStatus(me.getId(), status, pageable);
        } else {
            result = status == null ? swapRepository.findByRequesterId(me.getId(), pageable)
                    : swapRepository.findByRequesterIdAndStatus(me.getId(), status, pageable);
        }
        return PageResponse.of(result, s -> toResponse(s, me));
    }

    @Transactional(readOnly = true)
    public SwapResponse get(String email, Long id) {
        User me = userService.getByEmail(email);
        SwapRequest swap = find(id);
        requireParticipant(swap, me);
        return toResponse(swap, me);
    }

    @Transactional
    public SwapResponse updateStatus(String email, Long id, SwapStatus newStatus) {
        User me = userService.getByEmail(email);
        SwapRequest swap = find(id);
        requireParticipant(swap, me);

        boolean isReceiver = swap.getReceiver().getId().equals(me.getId());
        boolean isRequester = swap.getRequester().getId().equals(me.getId());
        SwapStatus current = swap.getStatus();

        switch (newStatus) {
            case ACCEPTED, REJECTED -> {
                if (!isReceiver) {
                    throw new ForbiddenException("Only the receiver can accept or reject a request");
                }
                requireStatus(current, SwapStatus.PENDING, newStatus);
                if (newStatus == SwapStatus.ACCEPTED) {
                    startLearningPeriod(swap);
                }
            }
            case CANCELLED -> {
                if (current == SwapStatus.PENDING) {
                    if (!isRequester) {
                        throw new ForbiddenException("Only the requester can cancel a request");
                    }
                } else if (current != SwapStatus.ACCEPTED) {
                    throw new ConflictException("A " + current + " request cannot be changed to " + newStatus);
                }

            }
            case COMPLETED -> throw new ConflictException(
                    "A swap is completed automatically when both people pass their final assessment");
            default -> throw new ConflictException("A request cannot be moved back to " + newStatus);
        }

        swap.setStatus(newStatus);
        return toResponse(swapRepository.save(swap), me);
    }

    @Transactional
    public SwapResponse finishEarly(String email, Long id) {
        User me = userService.getByEmail(email);
        SwapRequest swap = find(id);
        requireParticipant(swap, me);
        if (swap.getStatus() != SwapStatus.ACCEPTED) {
            throw new ConflictException("Only an accepted swap can be finished early");
        }
        if (swap.getRequester().getId().equals(me.getId())) {
            swap.setRequesterFinishedEarly(true);
        } else {
            swap.setReceiverFinishedEarly(true);
        }
        return toResponse(swapRepository.save(swap), me);
    }

    @Transactional
    public SwapResponse setMeetingLink(String email, Long id, String url) {
        User me = userService.getByEmail(email);
        SwapRequest swap = find(id);
        requireParticipant(swap, me);
        if (swap.getStatus() != SwapStatus.ACCEPTED) {
            throw new ConflictException("The video call link can be changed while the swap is accepted");
        }
        swap.setMeetingUrl(url.trim());
        return toResponse(swapRepository.save(swap), me);
    }

    private void startLearningPeriod(SwapRequest swap) {
        LocalDate today = LocalDate.now();
        swap.setStartDate(today);
        swap.setEndDate(today.plusDays(swap.getDurationDays()));
        swap.setMeetingUrl("https://meet.jit.si/SkillSwap-" + swap.getId() + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
    }

    private void requireStatus(SwapStatus current, SwapStatus expected, SwapStatus target) {
        if (current != expected) {
            throw new ConflictException("A " + current + " request cannot be changed to " + target);
        }
    }

    private void requireParticipant(SwapRequest swap, User me) {
        boolean participant = swap.getRequester().getId().equals(me.getId())
                || swap.getReceiver().getId().equals(me.getId());
        if (!participant) {
            throw new ForbiddenException("You are not part of this swap");
        }
    }

    private SwapRequest find(Long id) {
        return swapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found"));
    }

    private SwapResponse toResponse(SwapRequest swap, User me) {
        boolean reviewed = reviewRepository.existsBySwapRequestIdAndReviewerId(swap.getId(), me.getId());
        return SwapResponse.from(swap, reviewed, me.getId());
    }
}
