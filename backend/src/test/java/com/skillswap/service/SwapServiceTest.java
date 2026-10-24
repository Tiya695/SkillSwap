package com.skillswap.service;

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
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.SwapRequestRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.repository.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Optional;

import static com.skillswap.TestData.skill;
import static com.skillswap.TestData.swap;
import static com.skillswap.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SwapServiceTest {

    @Mock
    SwapRequestRepository swapRepository;
    @Mock
    UserSkillRepository userSkillRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    UserService userService;
    @Mock
    SkillService skillService;
    @InjectMocks
    SwapService swapService;

    User requester = user(1L, "Tiya");
    User receiver = user(2L, "Rahul");
    Skill python = skill(10L, "Python");
    Skill guitar = skill(11L, "Guitar");

    @BeforeEach
    void setUp() {
        when(userService.getByEmail(requester.getEmail())).thenReturn(requester);
        when(userService.getByEmail(receiver.getEmail())).thenReturn(receiver);
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(skillService.find(10L)).thenReturn(python);
        when(skillService.find(11L)).thenReturn(guitar);
        when(swapRepository.save(any(SwapRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(userSkillRepository.existsByUserIdAndSkillIdAndType(1L, 10L, SkillType.OFFERED)).thenReturn(true);
        when(userSkillRepository.existsByUserIdAndSkillIdAndType(2L, 11L, SkillType.OFFERED)).thenReturn(true);
    }

    private SwapCreateRequest request() {
        return new SwapCreateRequest(2L, 10L, 11L, 14, "Python for guitar?");
    }

    private SwapRequest swapWithStatus(SwapStatus status) {
        SwapRequest s = swap(5L, requester, receiver, python, guitar, status);
        when(swapRepository.findById(5L)).thenReturn(Optional.of(s));
        return s;
    }

    @Test
    void createsPendingRequest() {
        SwapResponse response = swapService.create(requester.getEmail(), request());

        assertThat(response.status()).isEqualTo(SwapStatus.PENDING);
        assertThat(response.requester().id()).isEqualTo(1L);
        assertThat(response.receiver().id()).isEqualTo(2L);
        assertThat(response.durationDays()).isEqualTo(14);
    }

    @Test
    void cannotSendRequestToYourself() {
        SwapCreateRequest toSelf = new SwapCreateRequest(1L, 10L, 11L, 14, null);

        assertThatThrownBy(() -> swapService.create(requester.getEmail(), toSelf))
                .isInstanceOf(BadRequestException.class);
        verify(swapRepository, never()).save(any());
    }

    @Test
    void offeredSkillMustBeInRequestersOfferedList() {
        when(userSkillRepository.existsByUserIdAndSkillIdAndType(1L, 10L, SkillType.OFFERED)).thenReturn(false);

        assertThatThrownBy(() -> swapService.create(requester.getEmail(), request()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("your own teaching list");
    }

    @Test
    void wantedSkillMustBeOfferedByReceiver() {
        when(userSkillRepository.existsByUserIdAndSkillIdAndType(2L, 11L, SkillType.OFFERED)).thenReturn(false);

        assertThatThrownBy(() -> swapService.create(requester.getEmail(), request()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not teach");
    }

    @Test
    void duplicatePendingRequestIsRejected() {
        when(swapRepository.existsByRequesterIdAndReceiverIdAndSkillOfferedIdAndSkillWantedIdAndStatus(
                1L, 2L, 10L, 11L, SwapStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> swapService.create(requester.getEmail(), request()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void receiverCanAcceptPendingRequest() {
        swapWithStatus(SwapStatus.PENDING);

        SwapResponse response = swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.ACCEPTED);

        assertThat(response.status()).isEqualTo(SwapStatus.ACCEPTED);
    }

    @Test
    void receiverCanRejectPendingRequest() {
        swapWithStatus(SwapStatus.PENDING);

        assertThat(swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.REJECTED).status())
                .isEqualTo(SwapStatus.REJECTED);
    }

    @Test
    void requesterCannotAcceptTheirOwnRequest() {
        swapWithStatus(SwapStatus.PENDING);

        assertThatThrownBy(() -> swapService.updateStatus(requester.getEmail(), 5L, SwapStatus.ACCEPTED))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void requesterCanCancelPendingRequest() {
        swapWithStatus(SwapStatus.PENDING);

        assertThat(swapService.updateStatus(requester.getEmail(), 5L, SwapStatus.CANCELLED).status())
                .isEqualTo(SwapStatus.CANCELLED);
    }

    @Test
    void receiverCannotCancel() {
        swapWithStatus(SwapStatus.PENDING);

        assertThatThrownBy(() -> swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.CANCELLED))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void acceptingStartsTheLearningPeriodWithACallLink() {
        SwapRequest swap = swapWithStatus(SwapStatus.PENDING);
        swap.setDurationDays(10);

        SwapResponse response = swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.ACCEPTED);

        assertThat(response.startDate()).isEqualTo(LocalDate.now());
        assertThat(response.endDate()).isEqualTo(LocalDate.now().plusDays(10));
        assertThat(response.meetingUrl()).startsWith("https://meet.jit.si/SkillSwap-5-");
        assertThat(response.assessmentUnlocked()).isFalse();
    }

    @Test
    void swapCannotBeCompletedByHand() {
        swapWithStatus(SwapStatus.ACCEPTED);

        assertThatThrownBy(() -> swapService.updateStatus(requester.getEmail(), 5L, SwapStatus.COMPLETED))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("automatically");
    }

    @Test
    void eitherPersonCanCancelAnAcceptedSwap() {
        swapWithStatus(SwapStatus.ACCEPTED);
        assertThat(swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.CANCELLED).status())
                .isEqualTo(SwapStatus.CANCELLED);

        swapWithStatus(SwapStatus.ACCEPTED);
        assertThat(swapService.updateStatus(requester.getEmail(), 5L, SwapStatus.CANCELLED).status())
                .isEqualTo(SwapStatus.CANCELLED);
    }

    @Test
    void assessmentsOpenWhenBothFinishEarly() {
        SwapRequest swap = swapWithStatus(SwapStatus.ACCEPTED);
        swap.setEndDate(LocalDate.now().plusDays(5));

        assertThat(swapService.finishEarly(requester.getEmail(), 5L).assessmentUnlocked()).isFalse();
        SwapResponse second = swapService.finishEarly(receiver.getEmail(), 5L);

        assertThat(second.assessmentUnlocked()).isTrue();
        assertThat(second.finishedEarlyByMe()).isTrue();
        assertThat(second.finishedEarlyByOther()).isTrue();
    }

    @Test
    void assessmentsOpenAfterTheEndDate() {
        SwapRequest swap = swapWithStatus(SwapStatus.ACCEPTED);
        swap.setEndDate(LocalDate.now());

        assertThat(swapService.get(requester.getEmail(), 5L).assessmentUnlocked()).isTrue();
    }

    @Test
    void callLinkCanBeReplacedOnlyWhileAccepted() {
        swapWithStatus(SwapStatus.ACCEPTED);
        assertThat(swapService.setMeetingLink(requester.getEmail(), 5L, " https://meet.google.com/abc-defg-hij ").meetingUrl())
                .isEqualTo("https://meet.google.com/abc-defg-hij");

        swapWithStatus(SwapStatus.PENDING);
        assertThatThrownBy(() -> swapService.setMeetingLink(requester.getEmail(), 5L, "https://meet.google.com/x"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotAcceptTwice() {
        swapWithStatus(SwapStatus.ACCEPTED);

        assertThatThrownBy(() -> swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.ACCEPTED))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotMoveBackToPending() {
        swapWithStatus(SwapStatus.ACCEPTED);

        assertThatThrownBy(() -> swapService.updateStatus(receiver.getEmail(), 5L, SwapStatus.PENDING))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void strangersCannotTouchTheSwap() {
        User stranger = user(9L, "Stranger");
        when(userService.getByEmail(stranger.getEmail())).thenReturn(stranger);
        swapWithStatus(SwapStatus.PENDING);

        assertThatThrownBy(() -> swapService.updateStatus(stranger.getEmail(), 5L, SwapStatus.ACCEPTED))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> swapService.get(stranger.getEmail(), 5L))
                .isInstanceOf(ForbiddenException.class);
        verify(reviewRepository, never()).existsBySwapRequestIdAndReviewerId(anyLong(), anyLong());
    }
}
