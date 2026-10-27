// Pure functions (no React, no fetch) so they are easy to test.
// Error keys use the same names as the server's "fieldErrors",
// so client errors and server errors can be shown in the same place.
import { LIMITS, QUESTION_TYPES, isChoiceType } from './constants';

/* ------------------------------------------------------------------ */
/* Survey form (create / edit)                                         */
/* ------------------------------------------------------------------ */

/**
 * Checks the survey form and returns an errors object.
 * An empty object {} means the form is valid.
 *
 * form = { title, description, questions: [{ text, type, required, options: ["Good", "Bad"] }] }
 * When questionsLocked is true the questions are not checked (the server ignores them).
 */
export function validateSurveyForm(form, questionsLocked = false) {
  const errors = {};

  const title = (form.title || '').trim();
  if (!title) {
    errors.title = 'Title is required';
  } else if (title.length < LIMITS.TITLE_MIN || title.length > LIMITS.TITLE_MAX) {
    errors.title = `Title must be between ${LIMITS.TITLE_MIN} and ${LIMITS.TITLE_MAX} characters`;
  }

  const description = (form.description || '').trim();
  if (description.length > LIMITS.DESCRIPTION_MAX) {
    errors.description = `Description can be at most ${LIMITS.DESCRIPTION_MAX} characters`;
  }

  if (questionsLocked) {
    return errors;
  }

  const questions = form.questions || [];
  if (questions.length < LIMITS.QUESTIONS_MIN) {
    errors.questions = 'Add at least one question';
  } else if (questions.length > LIMITS.QUESTIONS_MAX) {
    errors.questions = `A survey can have at most ${LIMITS.QUESTIONS_MAX} questions`;
  }

  questions.forEach((question, index) => {
    const textError = validateQuestionText(question.text);
    if (textError) {
      errors[`questions[${index}].text`] = textError;
    }

    if (!Object.values(QUESTION_TYPES).includes(question.type)) {
      errors[`questions[${index}].type`] = 'Please choose a question type';
    } else if (isChoiceType(question.type)) {
      const optionsError = validateOptions(question.options || []);
      if (optionsError) {
        errors[`questions[${index}].options`] = optionsError;
      }
    }
  });

  return errors;
}

function validateQuestionText(text) {
  const trimmed = (text || '').trim();
  if (!trimmed) {
    return 'Question text is required';
  }
  if (trimmed.length < LIMITS.QUESTION_TEXT_MIN || trimmed.length > LIMITS.QUESTION_TEXT_MAX) {
    return `Question text must be between ${LIMITS.QUESTION_TEXT_MIN} and ${LIMITS.QUESTION_TEXT_MAX} characters`;
  }
  return null;
}

function validateOptions(options) {
  const trimmed = options.map((option) => (option || '').trim());

  if (trimmed.length < LIMITS.OPTIONS_MIN || trimmed.length > LIMITS.OPTIONS_MAX) {
    return `Choice questions need between ${LIMITS.OPTIONS_MIN} and ${LIMITS.OPTIONS_MAX} options`;
  }
  if (trimmed.some((option) => option === '')) {
    return 'Options cannot be empty';
  }
  if (trimmed.some((option) => option.length > LIMITS.OPTION_TEXT_MAX)) {
    return `Each option can be at most ${LIMITS.OPTION_TEXT_MAX} characters`;
  }
  const lowerCase = trimmed.map((option) => option.toLowerCase());
  if (new Set(lowerCase).size !== lowerCase.length) {
    return 'Options must be unique';
  }
  return null;
}

/**
 * Turns the form into the SurveyRequest body the backend expects.
 * Text is trimmed and SHORT_TEXT questions always send an empty options list.
 */
export function buildSurveyRequest(form) {
  return {
    title: form.title.trim(),
    description: (form.description || '').trim(),
    questions: form.questions.map((question) => ({
      text: question.text.trim(),
      type: question.type,
      required: Boolean(question.required),
      options: isChoiceType(question.type) ? question.options.map((option) => option.trim()) : [],
    })),
  };
}

/* ------------------------------------------------------------------ */
/* Answers (take survey)                                               */
/* ------------------------------------------------------------------ */

// answers = { [questionId]: value }
//   SHORT_TEXT                      -> value is a string
//   SINGLE_CHOICE / MULTIPLE_CHOICE -> value is an array of option ids, e.g. [21] or [24, 25]

// True when the user has given some answer to this question.
export function isAnswered(question, value) {
  if (question.type === QUESTION_TYPES.SHORT_TEXT) {
    return typeof value === 'string' && value.trim() !== '';
  }
  return Array.isArray(value) && value.length > 0;
}

/**
 * Checks the answers and returns errors keyed like the server: { "answers.12": "This question is required" }.
 * An empty object {} means everything is fine.
 */
export function validateAnswers(questions, answers) {
  const errors = {};

  questions.forEach((question) => {
    const value = answers[question.id];
    const key = `answers.${question.id}`;

    if (!isAnswered(question, value)) {
      if (question.required) {
        errors[key] = 'This question is required';
      }
      return; // optional and empty is fine
    }

    if (question.type === QUESTION_TYPES.SHORT_TEXT && value.trim().length > LIMITS.ANSWER_TEXT_MAX) {
      errors[key] = `Answer can be at most ${LIMITS.ANSWER_TEXT_MAX} characters`;
    }
    if (question.type === QUESTION_TYPES.SINGLE_CHOICE && value.length !== 1) {
      errors[key] = 'Please choose exactly one option';
    }
    // MULTIPLE_CHOICE: at least one option is already checked by isAnswered()
  });

  return errors;
}

/**
 * Builds the SubmitRequest body. Only answered questions are included.
 * Result: { answers: [{ questionId: 10, text: "Ravi" }, { questionId: 11, optionIds: [21] }] }
 */
export function buildSubmitPayload(questions, answers) {
  const answerList = [];

  questions.forEach((question) => {
    const value = answers[question.id];
    if (!isAnswered(question, value)) {
      return;
    }
    if (question.type === QUESTION_TYPES.SHORT_TEXT) {
      answerList.push({ questionId: question.id, text: value.trim() });
    } else {
      answerList.push({ questionId: question.id, optionIds: [...value] });
    }
  });

  return { answers: answerList };
}
