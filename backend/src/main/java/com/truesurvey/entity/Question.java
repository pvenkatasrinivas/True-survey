package com.truesurvey.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * One question of a survey.
 * Many Questions belong to one Survey (many-to-one, column survey_id).
 * One Question has many options (only used by choice questions).
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false, length = 300)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Column(nullable = false)
    private boolean required;

    /** 1, 2, 3 ... - the order in which questions are shown. */
    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<QuestionOption>();

    public Question() {
    }

    public Question(String text, QuestionType type, boolean required, int position) {
        this.text = text;
        this.type = type;
        this.required = required;
        this.position = position;
    }

    public void addOption(QuestionOption option) {
        option.setQuestion(this);
        options.add(option);
    }

    public Long getId() {
        return id;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public String getText() {
        return text;
    }

    public QuestionType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public int getPosition() {
        return position;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }
}
