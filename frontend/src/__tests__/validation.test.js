import { describe, expect, it } from 'vitest';
import {
  buildSubmitPayload,
  buildSurveyRequest,
  validateAnswers,
  validateSurveyForm,
} from '../utils/validation';

// A valid form we can change a little in each test
function validForm() {
  return {
    title: 'Canteen Feedback',
    description: 'Tell us what you think',
    questions: [
      { text: 'Your name', type: 'SHORT_TEXT', required: false, options: [] },
      { text: 'How was the food?', type: 'SINGLE_CHOICE', required: true, options: ['Good', 'Poor'] },
    ],
  };
}

describe('validateSurveyForm', () => {
  it('returns no errors for a valid form', () => {
    expect(validateSurveyForm(validForm())).toEqual({});
  });

  it('requires a title', () => {
    const form = { ...validForm(), title: '   ' };
    expect(validateSurveyForm(form).title).toBe('Title is required');
  });

  it('checks title length (3-150)', () => {
    expect(validateSurveyForm({ ...validForm(), title: 'ab' }).title).toMatch(/between 3 and 150/);
    expect(validateSurveyForm({ ...validForm(), title: 'a'.repeat(151) }).title).toMatch(/between 3 and 150/);
    expect(validateSurveyForm({ ...validForm(), title: 'abc' }).title).toBeUndefined();
  });

  it('limits description to 1000 characters', () => {
    const form = { ...validForm(), description: 'x'.repeat(1001) };
    expect(validateSurveyForm(form).description).toMatch(/1000/);
  });

  it('needs at least one question and at most 50', () => {
    expect(validateSurveyForm({ ...validForm(), questions: [] }).questions).toBe('Add at least one question');

    const many = Array.from({ length: 51 }, () => ({
      text: 'Question',
      type: 'SHORT_TEXT',
      required: false,
      options: [],
    }));
    expect(validateSurveyForm({ ...validForm(), questions: many }).questions).toMatch(/at most 50/);
  });

  it('checks question text with the server key format', () => {
    const form = validForm();
    form.questions[1].text = 'Hi';
    const errors = validateSurveyForm(form);
    expect(errors['questions[1].text']).toMatch(/between 3 and 300/);
    expect(errors['questions[0].text']).toBeUndefined();
  });

  it('needs 2-10 options for choice questions', () => {
    const form = validForm();
    form.questions[1].options = ['Only one'];
    expect(validateSurveyForm(form)['questions[1].options']).toMatch(/between 2 and 10/);

    form.questions[1].options = Array.from({ length: 11 }, (_, i) => `Option ${i}`);
    expect(validateSurveyForm(form)['questions[1].options']).toMatch(/between 2 and 10/);
  });

  it('rejects empty and duplicate options', () => {
    const form = validForm();
    form.questions[1].options = ['Good', '  '];
    expect(validateSurveyForm(form)['questions[1].options']).toBe('Options cannot be empty');

    form.questions[1].options = ['Good', 'good '];
    expect(validateSurveyForm(form)['questions[1].options']).toBe('Options must be unique');
  });

  it('rejects options longer than 150 characters', () => {
    const form = validForm();
    form.questions[1].options = ['Good', 'x'.repeat(151)];
    expect(validateSurveyForm(form)['questions[1].options']).toMatch(/150/);
  });

  it('skips question checks when questions are locked', () => {
    const form = { ...validForm(), questions: [] };
    expect(validateSurveyForm(form, true)).toEqual({});
  });
});

describe('buildSurveyRequest', () => {
  it('trims text and sends no options for SHORT_TEXT', () => {
    const form = {
      title: '  Canteen  ',
      description: ' desc ',
      questions: [
        // options left over from when this was a choice question must not be sent
        { key: 'q1', text: ' Name ', type: 'SHORT_TEXT', required: false, options: ['old'] },
        { key: 'q2', text: 'Food?', type: 'SINGLE_CHOICE', required: true, options: [' Good ', 'Poor'] },
      ],
    };
    expect(buildSurveyRequest(form)).toEqual({
      title: 'Canteen',
      description: 'desc',
      questions: [
        { text: 'Name', type: 'SHORT_TEXT', required: false, options: [] },
        { text: 'Food?', type: 'SINGLE_CHOICE', required: true, options: ['Good', 'Poor'] },
      ],
    });
  });
});

// Questions in the same shape the server sends them
const questions = [
  { id: 10, text: 'Your name', type: 'SHORT_TEXT', required: false, options: [] },
  {
    id: 11,
    text: 'How was the food?',
    type: 'SINGLE_CHOICE',
    required: true,
    options: [
      { id: 21, text: 'Good' },
      { id: 22, text: 'Poor' },
    ],
  },
  {
    id: 12,
    text: 'Which meals?',
    type: 'MULTIPLE_CHOICE',
    required: true,
    options: [
      { id: 24, text: 'Breakfast' },
      { id: 25, text: 'Lunch' },
    ],
  },
  { id: 13, text: 'Comments', type: 'SHORT_TEXT', required: true, options: [] },
];

describe('validateAnswers', () => {
  it('returns no errors when all required questions are answered', () => {
    const answers = { 11: [21], 12: [24, 25], 13: 'Nice' };
    expect(validateAnswers(questions, answers)).toEqual({});
  });

  it('marks every missing required question using answers.<id> keys', () => {
    expect(validateAnswers(questions, {})).toEqual({
      'answers.11': 'This question is required',
      'answers.12': 'This question is required',
      'answers.13': 'This question is required',
    });
  });

  it('treats blank text as not answered', () => {
    const answers = { 11: [21], 12: [24], 13: '   ' };
    expect(validateAnswers(questions, answers)['answers.13']).toBe('This question is required');
  });

  it('lets optional questions stay empty', () => {
    const answers = { 10: '', 11: [21], 12: [24], 13: 'ok' };
    expect(validateAnswers(questions, answers)['answers.10']).toBeUndefined();
  });

  it('requires exactly one option for single choice', () => {
    const answers = { 11: [21, 22], 12: [24], 13: 'ok' };
    expect(validateAnswers(questions, answers)['answers.11']).toBe('Please choose exactly one option');
  });

  it('needs at least one option for multiple choice', () => {
    const answers = { 11: [21], 12: [], 13: 'ok' };
    expect(validateAnswers(questions, answers)['answers.12']).toBe('This question is required');
  });

  it('limits text answers to 1000 characters', () => {
    const answers = { 10: 'x'.repeat(1001), 11: [21], 12: [24], 13: 'ok' };
    expect(validateAnswers(questions, answers)['answers.10']).toMatch(/1000/);
  });
});

describe('buildSubmitPayload', () => {
  it('includes only answered questions, trims text and uses optionIds for choices', () => {
    const answers = { 10: '  Ravi  ', 11: [21], 12: [24, 25], 13: '   ' };
    expect(buildSubmitPayload(questions, answers)).toEqual({
      answers: [
        { questionId: 10, text: 'Ravi' },
        { questionId: 11, optionIds: [21] },
        { questionId: 12, optionIds: [24, 25] },
      ],
    });
  });

  it('returns an empty list when nothing is answered', () => {
    expect(buildSubmitPayload(questions, {})).toEqual({ answers: [] });
  });
});
