package com.truesurvey.dto;

import com.truesurvey.entity.QuestionOption;

public class OptionDto {

    private final Long id;
    private final String text;

    public OptionDto(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public static OptionDto from(QuestionOption option) {
        return new OptionDto(option.getId(), option.getText());
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
