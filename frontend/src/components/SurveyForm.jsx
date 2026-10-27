import { useState } from 'react';
import QuestionEditor from './QuestionEditor';
import ErrorMessage from './ErrorMessage';
import { LIMITS, QUESTION_TYPES, QUESTION_TYPE_LABELS, isChoiceType } from '../utils/constants';
import { buildSurveyRequest, validateSurveyForm } from '../utils/validation';

// Each question gets a "key" so React can track it in the list even after moving it.
let nextKey = 1;
function newKey() {
  nextKey += 1;
  return `q${nextKey}`;
}

function emptyQuestion() {
  return { key: newKey(), text: '', type: QUESTION_TYPES.SHORT_TEXT, required: false, options: [] };
}

/**
 * Turns a survey from the server into form values (used by the Edit page).
 * Server options look like { id, text } but the form only needs the text.
 */
export function surveyToFormValues(survey) {
  return {
    title: survey.title || '',
    description: survey.description || '',
    questions: (survey.questions || []).map((question) => ({
      key: newKey(),
      text: question.text,
      type: question.type,
      required: question.required,
      options: question.options.map((option) => option.text),
    })),
  };
}

/**
 * Form used for both "Create survey" and "Edit survey".
 *
 * Props:
 *  initialValues   - { title, description, questions } (optional; a new survey starts with one question)
 *  questionsLocked - true when the survey already has responses; questions are then read-only
 *  submitLabel     - text on the save button
 *  onSubmit        - async function(surveyRequest). If it throws, the error is shown in the form.
 *  onCancel        - called when "Cancel" is clicked
 */
function SurveyForm({ initialValues, questionsLocked = false, submitLabel = 'Save survey', onSubmit, onCancel }) {
  const [title, setTitle] = useState(initialValues?.title || '');
  const [description, setDescription] = useState(initialValues?.description || '');
  const [questions, setQuestions] = useState(initialValues?.questions || [emptyQuestion()]);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);

  function updateQuestion(index, updatedQuestion) {
    setQuestions(questions.map((q, i) => (i === index ? updatedQuestion : q)));
  }

  function addQuestion() {
    setQuestions([...questions, emptyQuestion()]);
  }

  function removeQuestion(index) {
    setQuestions(questions.filter((_, i) => i !== index));
    clearQuestionErrors(); // error keys use positions, which have now changed
  }

  // direction is -1 (up) or +1 (down)
  function moveQuestion(index, direction) {
    const target = index + direction;
    if (target < 0 || target >= questions.length) {
      return;
    }
    const copy = [...questions];
    [copy[index], copy[target]] = [copy[target], copy[index]];
    setQuestions(copy);
    clearQuestionErrors();
  }

  function clearQuestionErrors() {
    const remaining = {};
    Object.keys(errors).forEach((key) => {
      if (!key.startsWith('questions[')) {
        remaining[key] = errors[key];
      }
    });
    setErrors(remaining);
  }

  // Picks out the errors for one question, e.g. "questions[2].text" -> { text: "..." }
  function errorsForQuestion(index) {
    const prefix = `questions[${index}].`;
    const result = {};
    Object.keys(errors).forEach((key) => {
      if (key.startsWith(prefix)) {
        result[key.slice(prefix.length)] = errors[key];
      }
    });
    return result;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setServerError('');

    const form = { title, description, questions };
    const validationErrors = validateSurveyForm(form, questionsLocked);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      setServerError('Please fix the highlighted fields.');
      return;
    }

    setSaving(true);
    try {
      await onSubmit(buildSurveyRequest(form));
    } catch (error) {
      setServerError(error.message);
      setErrors(error.fieldErrors || {});
      setSaving(false);
    }
    // On success the page navigates away, so "saving" does not need to be reset.
  }

  return (
    <form className="survey-form" onSubmit={handleSubmit} noValidate>
      <ErrorMessage message={serverError} />

      <div className="card">
        <div className="form-group">
          <label htmlFor="survey-title">Title *</label>
          <input
            id="survey-title"
            type="text"
            value={title}
            maxLength={LIMITS.TITLE_MAX}
            placeholder="e.g. Canteen Feedback"
            onChange={(e) => setTitle(e.target.value)}
          />
          {errors.title && <p className="field-error">{errors.title}</p>}
        </div>

        <div className="form-group">
          <label htmlFor="survey-description">Description</label>
          <textarea
            id="survey-description"
            rows={3}
            value={description}
            maxLength={LIMITS.DESCRIPTION_MAX}
            placeholder="What is this survey about? (optional)"
            onChange={(e) => setDescription(e.target.value)}
          />
          {errors.description && <p className="field-error">{errors.description}</p>}
        </div>
      </div>

      <h2 className="section-title">Questions</h2>
      {errors.questions && <p className="field-error">{errors.questions}</p>}

      {questionsLocked ? (
        <LockedQuestions questions={questions} />
      ) : (
        <>
          {questions.map((question, index) => (
            <QuestionEditor
              key={question.key}
              question={question}
              index={index}
              total={questions.length}
              errors={errorsForQuestion(index)}
              onChange={(updated) => updateQuestion(index, updated)}
              onMoveUp={() => moveQuestion(index, -1)}
              onMoveDown={() => moveQuestion(index, 1)}
              onRemove={() => removeQuestion(index)}
            />
          ))}
          <button
            type="button"
            className="btn btn-secondary"
            onClick={addQuestion}
            disabled={questions.length >= LIMITS.QUESTIONS_MAX}
          >
            + Add question
          </button>
        </>
      )}

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : submitLabel}
        </button>
        {onCancel && (
          <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={saving}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}

// Read-only list of questions, shown when the survey already has responses.
function LockedQuestions({ questions }) {
  return (
    <div className="card">
      <p className="alert alert-info">This survey already has responses, so questions can't be changed.</p>
      <ol className="question-list">
        {questions.map((question) => (
          <li key={question.key}>
            <strong>{question.text}</strong>
            <span className="muted small">
              {' '}
              ({QUESTION_TYPE_LABELS[question.type]}
              {question.required ? ', required' : ''})
            </span>
            {isChoiceType(question.type) && (
              <ul className="option-list">
                {question.options.map((option) => (
                  <li key={option}>{option}</li>
                ))}
              </ul>
            )}
          </li>
        ))}
      </ol>
    </div>
  );
}

export default SurveyForm;
