import { LIMITS, QUESTION_TYPES } from '../utils/constants';

/**
 * Shows one question so a user can answer it (Take Survey page).
 *
 * value:
 *   SHORT_TEXT                      -> string
 *   SINGLE_CHOICE / MULTIPLE_CHOICE -> array of option ids
 * onChange(newValue) is called with the new value.
 */
function QuestionField({ question, number, value, error, onChange }) {
  const inputName = `question-${question.id}`;

  function handleCheckboxChange(optionId, checked) {
    const selected = value || [];
    if (checked) {
      onChange([...selected, optionId]);
    } else {
      onChange(selected.filter((id) => id !== optionId));
    }
  }

  let input;
  if (question.type === QUESTION_TYPES.SHORT_TEXT) {
    input = (
      <textarea
        id={inputName}
        name={inputName}
        rows={2}
        value={value || ''}
        maxLength={LIMITS.ANSWER_TEXT_MAX}
        placeholder="Your answer"
        onChange={(e) => onChange(e.target.value)}
      />
    );
  } else if (question.type === QUESTION_TYPES.SINGLE_CHOICE) {
    input = question.options.map((option) => (
      <label key={option.id} className="choice-label">
        <input
          type="radio"
          name={inputName}
          value={option.id}
          checked={(value || []).includes(option.id)}
          onChange={() => onChange([option.id])}
        />
        {option.text}
      </label>
    ));
  } else {
    input = question.options.map((option) => (
      <label key={option.id} className="choice-label">
        <input
          type="checkbox"
          name={inputName}
          value={option.id}
          checked={(value || []).includes(option.id)}
          onChange={(e) => handleCheckboxChange(option.id, e.target.checked)}
        />
        {option.text}
      </label>
    ));
  }

  const isText = question.type === QUESTION_TYPES.SHORT_TEXT;
  const questionTitle = (
    <>
      {number ? `${number}. ` : ''}
      {question.text}
      {question.required && (
        <span className="required-mark" title="Required">
          {' '}
          *
        </span>
      )}
    </>
  );

  return (
    <div className={`card question-field${error ? ' has-error' : ''}`}>
      {/* A text box gets a <label>; a group of radios/checkboxes gets a <fieldset> + <legend>. */}
      {isText ? (
        <>
          <label htmlFor={inputName} className="question-field-title">
            {questionTitle}
          </label>
          {input}
        </>
      ) : (
        <fieldset>
          <legend className="question-field-title">{questionTitle}</legend>
          {question.type === QUESTION_TYPES.MULTIPLE_CHOICE && (
            <p className="muted small">Select all that apply</p>
          )}
          <div className="choice-list">{input}</div>
        </fieldset>
      )}
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}

export default QuestionField;
