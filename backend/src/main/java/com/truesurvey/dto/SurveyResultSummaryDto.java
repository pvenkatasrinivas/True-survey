package com.truesurvey.dto;

import com.truesurvey.entity.QuestionType;

import java.util.ArrayList;
import java.util.List;

/** Result of GET /api/surveys/{id}/summary. */
public class SurveyResultSummaryDto {

    private final Long surveyId;
    private final String title;
    private final long totalResponses;
    private final List<QuestionSummary> questions = new ArrayList<QuestionSummary>();

    public SurveyResultSummaryDto(Long surveyId, String title, long totalResponses) {
        this.surveyId = surveyId;
        this.title = title;
        this.totalResponses = totalResponses;
    }

    public Long getSurveyId() {
        return surveyId;
    }

    public String getTitle() {
        return title;
    }

    public long getTotalResponses() {
        return totalResponses;
    }

    public List<QuestionSummary> getQuestions() {
        return questions;
    }

    /** Summary of one question. */
    public static class QuestionSummary {
        private final Long questionId;
        private final String text;
        private final QuestionType type;
        /** How many responses answered this question. */
        private final long answeredCount;
        private final List<OptionCount> options = new ArrayList<OptionCount>();
        private final List<String> textAnswers = new ArrayList<String>();

        public QuestionSummary(Long questionId, String text, QuestionType type, long answeredCount) {
            this.questionId = questionId;
            this.text = text;
            this.type = type;
            this.answeredCount = answeredCount;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public String getText() {
            return text;
        }

        public QuestionType getType() {
            return type;
        }

        public long getAnsweredCount() {
            return answeredCount;
        }

        public List<OptionCount> getOptions() {
            return options;
        }

        public List<String> getTextAnswers() {
            return textAnswers;
        }
    }

    /** How many people picked one option. */
    public static class OptionCount {
        private final Long optionId;
        private final String text;
        private final long count;
        /** count / answeredCount * 100, rounded to 1 decimal. */
        private final double percentage;

        public OptionCount(Long optionId, String text, long count, double percentage) {
            this.optionId = optionId;
            this.text = text;
            this.count = count;
            this.percentage = percentage;
        }

        public Long getOptionId() {
            return optionId;
        }

        public String getText() {
            return text;
        }

        public long getCount() {
            return count;
        }

        public double getPercentage() {
            return percentage;
        }
    }
}
