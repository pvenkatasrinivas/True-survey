package com.truesurvey.dto;

import com.truesurvey.entity.Question;
import com.truesurvey.entity.QuestionOption;
import com.truesurvey.entity.QuestionType;

import java.util.ArrayList;
import java.util.List;

public class QuestionDto {

    private Long id;
    private String text;
    private QuestionType type;
    private boolean required;
    private int position;
    private List<OptionDto> options;

    public static QuestionDto from(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.id = question.getId();
        dto.text = question.getText();
        dto.type = question.getType();
        dto.required = question.isRequired();
        dto.position = question.getPosition();
        dto.options = new ArrayList<OptionDto>();
        for (QuestionOption option : question.getOptions()) {
            dto.options.add(OptionDto.from(option));
        }
        return dto;
    }

    public Long getId() {
        return id;
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

    public List<OptionDto> getOptions() {
        return options;
    }
}
