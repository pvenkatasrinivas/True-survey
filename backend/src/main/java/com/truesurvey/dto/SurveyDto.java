package com.truesurvey.dto;

import com.truesurvey.entity.Question;
import com.truesurvey.entity.Survey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Full survey with its questions, returned by GET/POST/PUT /api/surveys/{id}. */
public class SurveyDto {

    private Long id;
    private String title;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private long responseCount;
    /** true when the survey has responses: questions can no longer be changed. */
    private boolean questionsLocked;
    private List<QuestionDto> questions;

    public static SurveyDto from(Survey survey, long responseCount) {
        SurveyDto dto = new SurveyDto();
        dto.id = survey.getId();
        dto.title = survey.getTitle();
        dto.description = survey.getDescription();
        dto.createdAt = survey.getCreatedAt();
        dto.updatedAt = survey.getUpdatedAt();
        dto.responseCount = responseCount;
        dto.questionsLocked = responseCount > 0;
        dto.questions = new ArrayList<QuestionDto>();
        for (Question question : survey.getQuestions()) {
            dto.questions.add(QuestionDto.from(question));
        }
        return dto;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getResponseCount() {
        return responseCount;
    }

    public boolean isQuestionsLocked() {
        return questionsLocked;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }
}
