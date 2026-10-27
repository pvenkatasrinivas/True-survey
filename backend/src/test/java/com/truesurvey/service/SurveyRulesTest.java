package com.truesurvey.service;

import com.truesurvey.dto.QuestionRequest;
import com.truesurvey.entity.QuestionType;
import com.truesurvey.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plain unit tests (no Spring, no database) for the business rules. */
class SurveyRulesTest {

    // The repositories are not used by validateQuestions, so null is fine here.
    private final SurveyService surveyService = new SurveyService(null, null, null);

    @Test
    void percentageIsRoundedToOneDecimal() {
        assertThat(ResponseService.percentage(1, 3)).isEqualTo(33.3);
        assertThat(ResponseService.percentage(2, 3)).isEqualTo(66.7);
        assertThat(ResponseService.percentage(3, 4)).isEqualTo(75.0);
    }

    @Test
    void percentageWithNoAnswersIsZero() {
        assertThat(ResponseService.percentage(0, 0)).isEqualTo(0.0);
    }

    @Test
    void validChoiceQuestionPasses() {
        QuestionRequest q = question(QuestionType.MULTIPLE_CHOICE, "Red", "Green", "Blue");
        surveyService.validateQuestions(Collections.singletonList(q)); // no exception
    }

    @Test
    void shortTextWithOptionsIsRejected() {
        QuestionRequest q = question(QuestionType.SHORT_TEXT, "A", "B");

        assertThatThrownBy(() -> surveyService.validateQuestions(Collections.singletonList(q)))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).getFieldErrors())
                        .containsEntry("questions[0].options", "Short text questions cannot have options"));
    }

    @Test
    void emptyOptionIsRejected() {
        QuestionRequest ok = question(QuestionType.SHORT_TEXT);
        QuestionRequest bad = question(QuestionType.SINGLE_CHOICE, "Yes", "  ");

        assertThatThrownBy(() -> surveyService.validateQuestions(Arrays.asList(ok, bad)))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).getFieldErrors())
                        .containsEntry("questions[1].options", "Options cannot be empty"));
    }

    private QuestionRequest question(QuestionType type, String... options) {
        QuestionRequest q = new QuestionRequest();
        q.setText("Sample question");
        q.setType(type);
        q.setOptions(Arrays.asList(options));
        return q;
    }
}
