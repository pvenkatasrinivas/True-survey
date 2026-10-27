// Values shared by many files. They match the rules in API_REFERENCE.md.

export const QUESTION_TYPES = {
  SHORT_TEXT: 'SHORT_TEXT',
  SINGLE_CHOICE: 'SINGLE_CHOICE',
  MULTIPLE_CHOICE: 'MULTIPLE_CHOICE',
};

// Friendly names shown in the UI
export const QUESTION_TYPE_LABELS = {
  SHORT_TEXT: 'Short text',
  SINGLE_CHOICE: 'Single choice',
  MULTIPLE_CHOICE: 'Multiple choice',
};

export const LIMITS = {
  TITLE_MIN: 3,
  TITLE_MAX: 150,
  DESCRIPTION_MAX: 1000,
  QUESTIONS_MIN: 1,
  QUESTIONS_MAX: 50,
  QUESTION_TEXT_MIN: 3,
  QUESTION_TEXT_MAX: 300,
  OPTIONS_MIN: 2,
  OPTIONS_MAX: 10,
  OPTION_TEXT_MAX: 150,
  ANSWER_TEXT_MAX: 1000,
};

// SINGLE_CHOICE and MULTIPLE_CHOICE questions have options, SHORT_TEXT does not.
export function isChoiceType(type) {
  return type === QUESTION_TYPES.SINGLE_CHOICE || type === QUESTION_TYPES.MULTIPLE_CHOICE;
}
