package com.skillswap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.dto.AssessmentDto.AnswerInput;
import com.skillswap.dto.AssessmentDto.CreateRequest;
import com.skillswap.dto.AssessmentDto.GenerateRequest;
import com.skillswap.dto.AssessmentDto.GeneratedQuestions;
import com.skillswap.dto.AssessmentDto.Overview;
import com.skillswap.dto.AssessmentDto.QuestionInput;
import com.skillswap.dto.AssessmentDto.QuestionView;
import com.skillswap.dto.AssessmentDto.SubmitRequest;
import com.skillswap.dto.AssessmentDto.View;
import com.skillswap.dto.SkillResponse;
import com.skillswap.dto.UserSummary;
import com.skillswap.entity.Assessment;
import com.skillswap.entity.AssessmentQuestion;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.enums.AssessmentStatus;
import com.skillswap.enums.SwapStatus;
import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.AssessmentRepository;
import com.skillswap.repository.SwapRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssessmentService {

    public static final int PASS_PERCENT = 50;
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final AssessmentRepository assessmentRepository;
    private final SwapRequestRepository swapRepository;
    private final SwapAccessService access;
    private final QuizGeneratorService quizGenerator;
    private final ObjectMapper objectMapper;

    public AssessmentService(AssessmentRepository assessmentRepository, SwapRequestRepository swapRepository,
                             SwapAccessService access, QuizGeneratorService quizGenerator, ObjectMapper objectMapper) {
        this.assessmentRepository = assessmentRepository;
        this.swapRepository = swapRepository;
        this.access = access;
        this.quizGenerator = quizGenerator;
        this.objectMapper = objectMapper;
    }

    public GeneratedQuestions generateDraft(String email, Long swapId, GenerateRequest request) {
        User me = access.me(email);
        SwapRequest swap = access.requireParticipant(me, swapId);
        requireUnlocked(swap);
        boolean iAmRequester = swap.getRequester().getId().equals(me.getId());
        String skillName = (iAmRequester ? swap.getSkillOffered() : swap.getSkillWanted()).getName();
        int count = request.count() == null ? 10 : request.count();

        List<QuestionInput> draft = quizGenerator.generate(skillName, request.topic().trim(), count).stream()
                .map(q -> new QuestionInput(q.text(), 10, q.options(), q.correctOption()))
                .toList();
        return new GeneratedQuestions(draft);
    }

    @Transactional(readOnly = true)
    public Overview overview(String email, Long swapId) {
        User me = access.me(email);
        SwapRequest swap = access.requireParticipant(me, swapId);
        List<Assessment> all = assessmentRepository.findBySwapRequestId(swapId);

        View toTeach = all.stream().filter(a -> a.getTeacher().getId().equals(me.getId())).findFirst()
                .map(a -> view(a, me)).orElse(null);
        View toTake = all.stream().filter(a -> a.getLearner().getId().equals(me.getId())).findFirst()
                .map(a -> view(a, me)).orElse(null);
        boolean unlocked = swap.isAssessmentUnlocked() || swap.getStatus() == SwapStatus.COMPLETED;
        return new Overview(unlocked, PASS_PERCENT, toTeach, toTake);
    }

    @Transactional
    public View create(String email, Long swapId, CreateRequest request) {
        User me = access.me(email);
        SwapRequest swap = access.requireParticipant(me, swapId);
        requireUnlocked(swap);

        boolean iAmRequester = swap.getRequester().getId().equals(me.getId());
        User learner = iAmRequester ? swap.getReceiver() : swap.getRequester();

        Assessment assessment = assessmentRepository.findBySwapRequestId(swapId).stream()
                .filter(a -> a.getTeacher().getId().equals(me.getId())).findFirst().orElse(null);
        if (assessment == null) {
            assessment = new Assessment();
            assessment.setSwapRequest(swap);
            assessment.setTeacher(me);
            assessment.setLearner(learner);

            assessment.setSkill(iAmRequester ? swap.getSkillOffered() : swap.getSkillWanted());
        } else if (assessment.getStatus() != AssessmentStatus.OPEN || assessment.getAttempt() > 1) {
            throw new ConflictException("This test was already taken, so it cannot be changed");
        } else {
            assessment.getQuestions().clear();
        }

        int position = 1;
        for (QuestionInput input : request.questions()) {
            validateChoices(position, input);
            AssessmentQuestion q = new AssessmentQuestion();
            q.setAssessment(assessment);
            q.setPosition(position++);
            q.setText(input.text().trim());
            q.setMaxPoints(input.maxPoints());
            q.setOptionsJson(writeOptions(input.options().stream().map(String::trim).toList()));
            q.setCorrectOption(input.correctOption());
            assessment.getQuestions().add(q);
        }
        return view(assessmentRepository.save(assessment), me);
    }

    @Transactional
    public View submit(String email, Long assessmentId, SubmitRequest request) {
        User me = access.me(email);
        Assessment assessment = load(assessmentId);
        if (!assessment.getLearner().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the person taking the test can answer it");
        }
        requireSwapAccepted(assessment);
        if (assessment.getStatus() != AssessmentStatus.OPEN) {
            throw new ConflictException("You already sent your answers for this test");
        }

        Map<Long, Integer> picks = new HashMap<>();
        for (AnswerInput a : request.answers()) {
            picks.put(a.questionId(), a.selectedOption());
        }

        int total = 0;
        int earned = 0;
        for (AssessmentQuestion q : assessment.getQuestions()) {
            Integer picked = picks.get(q.getId());
            int choices = readOptions(q).size();
            if (picked == null || picked < 0 || picked >= choices) {
                throw new BadRequestException("Please pick an answer for every question");
            }
            q.setSelectedOption(picked);
            boolean correct = picked.equals(q.getCorrectOption());
            q.setAwardedPoints(correct ? q.getMaxPoints() : 0);
            total += q.getMaxPoints();
            earned += q.getAwardedPoints();
        }

        int percent = total == 0 ? 0 : (int) Math.round(earned * 100.0 / total);
        assessment.setScorePercent(percent);
        assessment.setPassed(percent >= PASS_PERCENT);
        assessment.setStatus(AssessmentStatus.GRADED);
        Assessment saved = assessmentRepository.save(assessment);

        completeSwapIfBothPassed(saved.getSwapRequest().getId());
        return view(saved, me);
    }

    @Transactional
    public View allowRetake(String email, Long assessmentId) {
        User me = access.me(email);
        Assessment assessment = load(assessmentId);
        if (!assessment.getTeacher().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the person who wrote the test can allow a retake");
        }
        requireSwapAccepted(assessment);
        if (assessment.getStatus() != AssessmentStatus.GRADED || Boolean.TRUE.equals(assessment.getPassed())) {
            throw new ConflictException("A retake is only possible after a failed test");
        }
        for (AssessmentQuestion q : assessment.getQuestions()) {
            q.setSelectedOption(null);
            q.setAwardedPoints(null);
        }
        assessment.setScorePercent(null);
        assessment.setPassed(null);
        assessment.setAttempt(assessment.getAttempt() + 1);
        assessment.setStatus(AssessmentStatus.OPEN);
        return view(assessmentRepository.save(assessment), me);
    }

    private void requireUnlocked(SwapRequest swap) {
        if (!swap.isAssessmentUnlocked()) {
            throw new ConflictException(
                    "Tests open when the learning period ends, or when both of you choose to finish early");
        }
    }

    private void validateChoices(int position, QuestionInput input) {
        if (input.options().stream().anyMatch(o -> o == null || o.isBlank())) {
            throw new BadRequestException("Question " + position + " has an empty answer choice");
        }
        if (input.correctOption() == null || input.correctOption() < 0 || input.correctOption() >= input.options().size()) {
            throw new BadRequestException("Mark which answer choice is correct for question " + position);
        }
    }

    private void completeSwapIfBothPassed(Long swapId) {
        List<Assessment> all = assessmentRepository.findBySwapRequestId(swapId);
        boolean bothPassed = all.size() == 2 && all.stream().allMatch(a -> Boolean.TRUE.equals(a.getPassed()));
        if (bothPassed) {
            SwapRequest swap = swapRepository.findById(swapId).orElseThrow();
            swap.setStatus(SwapStatus.COMPLETED);
            swapRepository.save(swap);
        }
    }

    private Assessment load(Long id) {
        return assessmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));
    }

    private void requireSwapAccepted(Assessment assessment) {
        if (assessment.getSwapRequest().getStatus() != SwapStatus.ACCEPTED) {
            throw new ConflictException("This swap is no longer active");
        }
    }

    private View view(Assessment a, User me) {
        boolean iAmTeacher = a.getTeacher().getId().equals(me.getId());
        boolean showAnswers = iAmTeacher || a.getStatus() == AssessmentStatus.GRADED;
        List<QuestionView> questions = a.getQuestions().stream()
                .map(q -> new QuestionView(q.getId(), q.getPosition(), q.getText(), q.getMaxPoints(), readOptions(q),
                        q.getSelectedOption(), showAnswers ? q.getCorrectOption() : null,
                        a.getStatus() == AssessmentStatus.GRADED ? q.getAwardedPoints() : null))
                .toList();
        return new View(a.getId(), a.getSwapRequest().getId(), UserSummary.from(a.getTeacher()),
                UserSummary.from(a.getLearner()), SkillResponse.from(a.getSkill()), a.getStatus(), a.getAttempt(),
                a.getScorePercent(), a.getPassed(), questions);
    }

    private List<String> readOptions(AssessmentQuestion q) {
        if (q.getOptionsJson() == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(q.getOptionsJson(), STRING_LIST);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored answer choices are not readable", e);
        }
    }

    private String writeOptions(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store the answer choices", e);
        }
    }
}
