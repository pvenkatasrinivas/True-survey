package com.truesurvey.entity;

/** The kinds of questions a survey can have. */
public enum QuestionType {
    /** Free text answer. */
    SHORT_TEXT,
    /** Pick exactly one option (radio buttons). */
    SINGLE_CHOICE,
    /** Pick one or more options (checkboxes). */
    MULTIPLE_CHOICE;

    public boolean hasOptions() {
        return this == SINGLE_CHOICE || this == MULTIPLE_CHOICE;
    }
}
