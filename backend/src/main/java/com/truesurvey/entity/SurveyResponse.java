package com.truesurvey.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One submission of a survey by one participant.
 * Many responses belong to one survey; one response has many answers.
 */
@Entity
@Table(name = "survey_responses")
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<Answer>();

    public SurveyResponse() {
    }

    public SurveyResponse(Survey survey) {
        this.survey = survey;
    }

    @PrePersist
    public void onCreate() {
        this.submittedAt = Instant.now();
    }

    public void addAnswer(Answer answer) {
        answer.setResponse(this);
        answers.add(answer);
    }

    public Long getId() {
        return id;
    }

    public Survey getSurvey() {
        return survey;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
