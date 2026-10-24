package com.skillswap.controller;

import com.skillswap.dto.AssessmentDto;
import com.skillswap.dto.ChatMessageRequest;
import com.skillswap.dto.ChatMessageResponse;
import com.skillswap.dto.MeetingLinkRequest;
import com.skillswap.dto.SwapResponse;
import com.skillswap.service.AssessmentService;
import com.skillswap.service.ChatService;
import com.skillswap.service.SwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Swap workspace", description = "Chat, video call link and final assessments of an accepted swap")
public class SwapLearningController {

    private final SwapService swapService;
    private final ChatService chatService;
    private final AssessmentService assessmentService;

    public SwapLearningController(SwapService swapService, ChatService chatService,
                                  AssessmentService assessmentService) {
        this.swapService = swapService;
        this.chatService = chatService;
        this.assessmentService = assessmentService;
    }

    @GetMapping("/swaps/{id}/messages")
    @Operation(summary = "Chat messages of a swap, only the ones after afterId (oldest first)")
    public List<ChatMessageResponse> messages(Principal principal, @PathVariable Long id,
                                              @RequestParam(defaultValue = "0") Long afterId) {
        return chatService.list(principal.getName(), id, afterId);
    }

    @PostMapping("/swaps/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a chat message (accepted swaps only)")
    public ChatMessageResponse send(Principal principal, @PathVariable Long id,
                                    @Valid @RequestBody ChatMessageRequest request) {
        return chatService.send(principal.getName(), id, request);
    }

    @PatchMapping("/swaps/{id}/meeting-link")
    @Operation(summary = "Replace the video call link with your own https link")
    public SwapResponse meetingLink(Principal principal, @PathVariable Long id,
                                    @Valid @RequestBody MeetingLinkRequest request) {
        return swapService.setMeetingLink(principal.getName(), id, request.url());
    }

    @PostMapping("/swaps/{id}/finish-early")
    @Operation(summary = "Say you are ready for the assessments before the end date; they open when both of you did")
    public SwapResponse finishEarly(Principal principal, @PathVariable Long id) {
        return swapService.finishEarly(principal.getName(), id);
    }

    @GetMapping("/swaps/{id}/assessments")
    @Operation(summary = "The test I give and the test I take in this swap")
    public AssessmentDto.Overview assessments(Principal principal, @PathVariable Long id) {
        return assessmentService.overview(principal.getName(), id);
    }

    @PostMapping("/swaps/{id}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Write the multiple choice test for the other person (opens after the learning period)")
    public AssessmentDto.View createAssessment(Principal principal, @PathVariable Long id,
                                               @Valid @RequestBody AssessmentDto.CreateRequest request) {
        return assessmentService.create(principal.getName(), id, request);
    }

    @PostMapping("/swaps/{id}/assessments/generate")
    @Operation(summary = "Draft multiple choice questions about a topic with the local AI model (nothing is saved)")
    public AssessmentDto.GeneratedQuestions generateQuestions(Principal principal, @PathVariable Long id,
                                                              @Valid @RequestBody AssessmentDto.GenerateRequest request) {
        return assessmentService.generateDraft(principal.getName(), id, request);
    }

    @PostMapping("/assessments/{id}/submit")
    @Operation(summary = "Send my answers. The app marks them at once: points, percentage, passed or not")
    public AssessmentDto.View submit(Principal principal, @PathVariable Long id,
                                     @Valid @RequestBody AssessmentDto.SubmitRequest request) {
        return assessmentService.submit(principal.getName(), id, request);
    }

    @PostMapping("/assessments/{id}/retake")
    @Operation(summary = "Let the learner take the test again after a failed attempt")
    public AssessmentDto.View retake(Principal principal, @PathVariable Long id) {
        return assessmentService.allowRetake(principal.getName(), id);
    }
}
