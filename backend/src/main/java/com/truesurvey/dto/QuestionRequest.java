package com.truesurvey.dto;

import com.truesurvey.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/** One question inside a SurveyRequest. Option rules are checked in SurveyService. */
public class QuestionRequest {

    @Schema(example = "How was the food?")
    @NotBlank(message = "Question text is required")
    @Size(min = 3, max = 300, message = "Question text must be between 3 and 300 characters")
    private String text;

    @Schema(example = "SINGLE_CHOICE")
    @NotNull(message = "Question type is required")
    private QuestionType type;

    @Schema(example = "true")
    private boolean required;

    @Schema(example = "[\"Good\", \"Average\", \"Poor\"]")
    private List<String> options = new ArrayList<String>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
