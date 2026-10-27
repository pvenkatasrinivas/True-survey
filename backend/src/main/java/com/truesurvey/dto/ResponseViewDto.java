package com.truesurvey.dto;

import java.time.Instant;
import java.util.List;

/** One submitted response, ready to display (question text + answer as text). */
public class ResponseViewDto {

    private final Long id;
    private final Instant submittedAt;
    private final List<AnswerView> answers;

    public ResponseViewDto(Long id, Instant submittedAt, List<AnswerView> answers) {
        this.id = id;
        this.submittedAt = submittedAt;
        this.answers = answers;
    }

    public Long getId() {
        return id;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public List<AnswerView> getAnswers() {
        return answers;
    }

    public static class AnswerView {
        private final Long questionId;
        private final String questionText;
        /** Text answer, or the selected options joined with ", ". */
        private final String answer;

        public AnswerView(Long questionId, String questionText, String answer) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.answer = answer;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
