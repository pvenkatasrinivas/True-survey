package com.truesurvey.dto;

import com.truesurvey.entity.Survey;

import java.time.Instant;

/** Short survey info for the list page (no questions). */
public class SurveySummaryDto {

    private final Long id;
    private final String title;
    private final String description;
    private final long questionCount;
    private final long responseCount;
    private final Instant createdAt;

    public SurveySummaryDto(Survey survey, long questionCount, long responseCount) {
        this.id = survey.getId();
        this.title = survey.getTitle();
        this.description = survey.getDescription();
        this.questionCount = questionCount;
        this.responseCount = responseCount;
        this.createdAt = survey.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getQuestionCount() {
        return questionCount;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
