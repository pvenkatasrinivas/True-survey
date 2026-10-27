package com.truesurvey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/** The answer to one question. Use "text" for SHORT_TEXT and "optionIds" for choice questions. */
public class AnswerRequest {

    @Schema(example = "11")
    @NotNull(message = "questionId is required")
    private Long questionId;

    @Schema(example = "Very tasty")
    private String text;

    @Schema(example = "[21]")
    private List<Long> optionIds = new ArrayList<Long>();

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Long> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(List<Long> optionIds) {
        this.optionIds = optionIds;
    }
}
