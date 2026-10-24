package com.skillswap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.dto.AssessmentDto.AnswerInput;
import com.skillswap.dto.AssessmentDto.CreateRequest;
import com.skillswap.dto.AssessmentDto.GenerateRequest;
import com.skillswap.dto.AssessmentDto.QuestionInput;
import com.skillswap.dto.AssessmentDto.SubmitRequest;
import com.skillswap.dto.AssessmentDto.View;
import com.skillswap.entity.Assessment;
import com.skillswap.entity.AssessmentQuestion;
import com.skillswap.entity.Skill;
import com.skillswap.entity.SwapRequest;
import com.skillswap.entity.User;
import com.skillswap.enums.AssessmentStatus;
import com.skillswap.enums.SwapStatus;
import com.skillswap.exception.BadRequestException;
import com.skillswap.exception.ConflictException;
import com.skillswap.exception.ForbiddenException;
import com.skillswap.repository.AssessmentRepository;
import com.skillswap.repository.SwapRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.skillswap.TestData.skill;
import static com.skillswap.TestData.swap;
import static com.skillswap.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssessmentServiceTest {

    @Mock
    AssessmentRepository assessmentRepository;
    @Mock
    SwapRequestRepository swapRepository;
    @Mock
    SwapAccessService access;
    @Mock
    QuizGeneratorService quizGenerator;

    AssessmentService service;

    User tiya = user(1L, "Tiya");
    User rahul = user(2L, "Rahul");
    Skill python = skill(10L, "Python");
    Skill guitar = skill(11L, "Guitar");
    SwapRequest swap;

    @BeforeEach
    void setUp() {
        service = new AssessmentService(assessmentRepository, swapRepository, access, quizGenerator, new ObjectMapper());
        swap = swap(5L, tiya, rahul, python, guitar, SwapStatus.ACCEPTED);
        swap.setEndDate(LocalDate.now());
        when(access.me(tiya.getEmail())).thenReturn(tiya);
        when(access.me(rahul.getEmail())).thenReturn(rahul);
        when(access.requireParticipant(any(User.class), org.mockito.ArgumentMatchers.eq(5L))).thenReturn(swap);
        when(swapRepository.findById(5L)).thenReturn(Optional.of(swap));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(i -> i.getArgument(0));
        when(assessmentRepository.findBySwapRequestId(5L)).thenReturn(List.of());
    }

    private static QuestionInput mcq(String text, int points, int correct) {
        return new QuestionInput(text, points, List.of("one", "two", "three", "four"), correct);
    }

    private Assessment quiz(AssessmentStatus status) {
        Assessment a = new Assessment();
        a.setId(50L);
        a.setSwapRequest(swap);
        a.setTeacher(tiya);
        a.setLearner(rahul);
        a.setSkill(python);
        a.setStatus(status);
        int[] correct = {1, 2};
        for (int i = 0; i < 2; i++) {
            AssessmentQuestion q = new AssessmentQuestion();
            q.setId(101L + i);
            q.setAssessment(a);
            q.setPosition(i + 1);
            q.setText("Question " + (i + 1));
            q.setMaxPoints(10);
            q.setOptionsJson("[\"one\",\"two\",\"three\",\"four\"]");
            q.setCorrectOption(correct[i]);
            a.getQuestions().add(q);
        }
        when(assessmentRepository.findWithDetailsById(50L)).thenReturn(Optional.of(a));
        return a;
    }

    private SubmitRequest picks(Integer first, Integer second) {
        return new SubmitRequest(List.of(new AnswerInput(101L, first), new AnswerInput(102L, second)));
    }

    @Test
    void teacherCanWriteTheTestOnceTheLearningPeriodIsOver() {
        View view = service.create(tiya.getEmail(), 5L,
                new CreateRequest(List.of(mcq("What is a list?", 10, 1), mcq("Write a loop", 20, 3))));

        assertThat(view.status()).isEqualTo(AssessmentStatus.OPEN);
        assertThat(view.skill().name()).isEqualTo("Python");
        assertThat(view.learner().id()).isEqualTo(2L);
        assertThat(view.questions()).hasSize(2);
        assertThat(view.questions().get(0).options()).containsExactly("one", "two", "three", "four");
        assertThat(view.questions().get(1).correctOption()).isEqualTo(3);
    }

    @Test
    void receiverTeachesTheWantedSkill() {
        View view = service.create(rahul.getEmail(), 5L, new CreateRequest(List.of(mcq("Name a chord", 10, 0))));

        assertThat(view.skill().name()).isEqualTo("Guitar");
        assertThat(view.learner().id()).isEqualTo(1L);
    }

    @Test
    void theCorrectChoiceMustBeOneOfTheChoices() {
        assertThatThrownBy(() -> service.create(tiya.getEmail(), 5L, new CreateRequest(List.of(mcq("Q one", 10, 4)))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("correct");
    }

    @Test
    void emptyChoicesAreRefused() {
        QuestionInput blank = new QuestionInput("A question", 10, List.of("yes", " "), 0);

        assertThatThrownBy(() -> service.create(tiya.getEmail(), 5L, new CreateRequest(List.of(blank))))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("empty");
    }

    @Test
    void testsAreLockedDuringTheLearningPeriod() {
        swap.setEndDate(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> service.create(tiya.getEmail(), 5L, new CreateRequest(List.of(mcq("Q one", 10, 0)))))
                .isInstanceOf(ConflictException.class).hasMessageContaining("learning period");
    }

    @Test
    void allRightAnswersGiveFullPointsAndPass() {
        quiz(AssessmentStatus.OPEN);

        View view = service.submit(rahul.getEmail(), 50L, picks(1, 2));

        assertThat(view.status()).isEqualTo(AssessmentStatus.GRADED);
        assertThat(view.scorePercent()).isEqualTo(100);
        assertThat(view.passed()).isTrue();
        assertThat(view.questions()).extracting("awardedPoints").containsExactly(10, 10);
    }

    @Test
    void exactlyFiftyPercentPasses() {
        quiz(AssessmentStatus.OPEN);

        View view = service.submit(rahul.getEmail(), 50L, picks(1, 0));

        assertThat(view.scorePercent()).isEqualTo(50);
        assertThat(view.passed()).isTrue();
    }

    @Test
    void belowFiftyPercentFailsAndTheSwapStaysOpen() {
        Assessment a = quiz(AssessmentStatus.OPEN);
        a.getQuestions().get(0).setMaxPoints(10);
        a.getQuestions().get(1).setMaxPoints(30);

        View view = service.submit(rahul.getEmail(), 50L, picks(1, 0));

        assertThat(view.scorePercent()).isEqualTo(25);
        assertThat(view.passed()).isFalse();
        assertThat(view.questions()).extracting("awardedPoints").containsExactly(10, 0);
        assertThat(swap.getStatus()).isEqualTo(SwapStatus.ACCEPTED);
    }

    @Test
    void everyQuestionMustBeAnswered() {
        quiz(AssessmentStatus.OPEN);

        assertThatThrownBy(() -> service.submit(rahul.getEmail(), 50L, picks(1, null)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("every question");
        assertThatThrownBy(() -> service.submit(rahul.getEmail(), 50L, picks(1, 9)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void onlyTheLearnerCanAnswerAndOnlyOnce() {
        quiz(AssessmentStatus.OPEN);
        assertThatThrownBy(() -> service.submit(tiya.getEmail(), 50L, picks(1, 2))).isInstanceOf(ForbiddenException.class);

        service.submit(rahul.getEmail(), 50L, picks(1, 2));
        assertThatThrownBy(() -> service.submit(rahul.getEmail(), 50L, picks(1, 2))).isInstanceOf(ConflictException.class);
    }

    @Test
    void learnerDoesNotSeeTheCorrectAnswersBeforeTakingTheTest() {
        Assessment a = quiz(AssessmentStatus.OPEN);
        when(assessmentRepository.findBySwapRequestId(5L)).thenReturn(List.of(a));

        var forLearner = service.overview(rahul.getEmail(), 5L).toTake();
        var forTeacher = service.overview(tiya.getEmail(), 5L).toTeach();

        assertThat(forLearner.questions()).allMatch(q -> q.correctOption() == null && q.awardedPoints() == null);
        assertThat(forTeacher.questions()).allMatch(q -> q.correctOption() != null);
    }

    @Test
    void swapCompletesOnlyWhenBothTestsArePassed() {
        Assessment mine = quiz(AssessmentStatus.OPEN);

        Assessment other = new Assessment();
        other.setSwapRequest(swap);
        other.setTeacher(rahul);
        other.setLearner(tiya);
        other.setPassed(true);
        when(assessmentRepository.findBySwapRequestId(5L)).thenReturn(List.of(mine, other));

        service.submit(rahul.getEmail(), 50L, picks(1, 2));

        assertThat(swap.getStatus()).isEqualTo(SwapStatus.COMPLETED);
        verify(swapRepository).save(swap);
    }

    @Test
    void retakeClearsTheAnswersAndCountsTheAttempt() {
        quiz(AssessmentStatus.OPEN);
        service.submit(rahul.getEmail(), 50L, picks(0, 0));

        View view = service.allowRetake(tiya.getEmail(), 50L);

        assertThat(view.status()).isEqualTo(AssessmentStatus.OPEN);
        assertThat(view.attempt()).isEqualTo(2);
        assertThat(view.questions()).allMatch(q -> q.selectedOption() == null && q.awardedPoints() == null);
    }

    @Test
    void passedTestCannotBeRetaken() {
        quiz(AssessmentStatus.OPEN);
        service.submit(rahul.getEmail(), 50L, picks(1, 2));

        assertThatThrownBy(() -> service.allowRetake(tiya.getEmail(), 50L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void onlyTheWriterCanAllowARetake() {
        quiz(AssessmentStatus.OPEN);
        service.submit(rahul.getEmail(), 50L, picks(0, 0));

        assertThatThrownBy(() -> service.allowRetake(rahul.getEmail(), 50L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void draftQuestionsAreAskedForTheSkillTheTeacherTeaches() {
        var q = new QuizGeneratorService.GeneratedQuestion("What is a list?", List.of("a", "b", "c", "d"), 2);
        when(quizGenerator.generate("Python", "Lists and loops", 10)).thenReturn(List.of(q));
        when(quizGenerator.generate("Guitar", "Chords", 3)).thenReturn(List.of(q));

        var draft = service.generateDraft(tiya.getEmail(), 5L, new GenerateRequest(" Lists and loops ", null));
        assertThat(draft.questions()).hasSize(1);
        assertThat(draft.questions().get(0).options()).containsExactly("a", "b", "c", "d");
        assertThat(draft.questions().get(0).correctOption()).isEqualTo(2);
        assertThat(draft.questions().get(0).maxPoints()).isEqualTo(10);

        assertThat(service.generateDraft(rahul.getEmail(), 5L, new GenerateRequest("Chords", 3)).questions()).hasSize(1);
    }

    @Test
    void draftQuestionsAreLockedLikeTheTest() {
        swap.setEndDate(LocalDate.now().plusDays(2));

        assertThatThrownBy(() -> service.generateDraft(tiya.getEmail(), 5L, new GenerateRequest("Lists", 5)))
                .isInstanceOf(ConflictException.class);
    }
}
