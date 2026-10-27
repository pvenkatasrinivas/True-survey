package com.truesurvey.dto;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/** Body of POST /api/surveys/{id}/responses. Optional questions may be left out. */
public class SubmitResponseRequest {

    @Valid
    private List<AnswerRequest> answers = new ArrayList<AnswerRequest>();

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }
}
