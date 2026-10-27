package com.truesurvey.dto;

public class SubmitResponseResult {

    private final Long responseId;
    private final String message;

    public SubmitResponseResult(Long responseId, String message) {
        this.responseId = responseId;
        this.message = message;
    }

    public Long getResponseId() {
        return responseId;
    }

    public String getMessage() {
        return message;
    }
}
