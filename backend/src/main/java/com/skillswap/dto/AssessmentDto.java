package com.skillswap.dto;

import com.skillswap.enums.AssessmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AssessmentDto {

    private AssessmentDto() {
    }

    public record QuestionInput(
            @NotBlank(message = "Write the question") @Size(max = 500, message = "A question can have at most 500 characters") String text,
            @Min(value = 1, message = "Points must be at least 1") @Max(value = 100, message = "Points can be at most 100") int maxPoints,
            @NotEmpty(message = "Add the answer choices") @Size(min = 2, max = 6, message = "A question needs 2 to 6 answer choices") List<
                    @NotBlank(message = "An answer choice cannot be empty") @Size(max = 200, message = "An answer choice can have at most 200 characters") String> options,
            @NotNull(message = "Mark the correct answer") @Min(value = 0, message = "Mark the correct answer") Integer correctOption) {
    }

    public record CreateRequest(
            @NotEmpty(message = "Add at least one question") @Size(max = 10, message = "At most 10 questions") @Valid List<QuestionInput> questions) {
    }

    public record GenerateRequest(
            @NotBlank(message = "Write the topic of the test") @Size(max = 200, message = "The topic can have at most 200 characters") String topic,
            @Min(value = 1, message = "Ask for at least 1 question") @Max(value = 10, message = "Ask for at most 10 questions") Integer count) {
    }

    public record GeneratedQuestions(List<QuestionInput> questions) {
    }

    public record AnswerInput(@NotNull(message = "Question is required") Long questionId,
                              @NotNull(message = "Pick an answer for every question") Integer selectedOption) {
    }

    public record SubmitRequest(@NotEmpty(message = "Answer the questions") @Valid List<AnswerInput> answers) {
    }

    public record QuestionView(Long id, int position, String text, int maxPoints, List<String> options,
                               Integer selectedOption, Integer correctOption, Integer awardedPoints) {
    }

    public record View(Long id, Long swapId, UserSummary teacher, UserSummary learner, SkillResponse skill,
                       AssessmentStatus status, int attempt, Integer scorePercent, Boolean passed,
                       List<QuestionView> questions) {
    }

    public record Overview(boolean unlocked, int passMark, View toTeach, View toTake) {
    }
}
