package com.truesurvey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/** Body for creating or updating a survey. @Valid on the list also validates every question. */
public class SurveyRequest {

    @Schema(example = "Canteen Feedback")
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Schema(example = "Tell us what you think about the canteen")
    @Size(max = 1000, message = "Description can be at most 1000 characters")
    private String description;

    @NotNull(message = "Add at least one question")
    @Size(min = 1, max = 50, message = "A survey needs between 1 and 50 questions")
    @Valid
    private List<QuestionRequest> questions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionRequest> questions) {
        this.questions = questions;
    }
}
