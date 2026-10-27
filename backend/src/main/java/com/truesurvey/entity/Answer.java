package com.truesurvey.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * One stored answer.
 * - SHORT_TEXT question      -> one row with textValue filled.
 * - SINGLE_CHOICE question   -> one row with option filled.
 * - MULTIPLE_CHOICE question -> one row per selected option.
 * Keeping one row per selected option makes counting option votes a simple COUNT.
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Selected option (choice questions only). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private QuestionOption option;

    /** Typed answer (text questions only). */
    @Column(name = "text_value", length = 1000)
    private String textValue;

    public Answer() {
    }

    public static Answer text(Question question, String text) {
        Answer answer = new Answer();
        answer.question = question;
        answer.textValue = text;
        return answer;
    }

    public static Answer choice(Question question, QuestionOption option) {
        Answer answer = new Answer();
        answer.question = question;
        answer.option = option;
        return answer;
    }

    public Long getId() {
        return id;
    }

    public SurveyResponse getResponse() {
        return response;
    }

    public void setResponse(SurveyResponse response) {
        this.response = response;
    }

    public Question getQuestion() {
        return question;
    }

    public QuestionOption getOption() {
        return option;
    }

    public String getTextValue() {
        return textValue;
    }
}
