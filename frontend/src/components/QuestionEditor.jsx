import { LIMITS, QUESTION_TYPES, QUESTION_TYPE_LABELS, isChoiceType } from '../utils/constants';

/**
 * Edits one question inside the survey form.
 *
 * question = { key, text, type, required, options: ["Good", "Bad"] }
 * errors   = { text, type, options } (each optional)
 * onChange(updatedQuestion) is called with a new question object on every change.
 */
function QuestionEditor({ question, index, total, errors = {}, onChange, onMoveUp, onMoveDown, onRemove }) {
  const number = index + 1;
  const showOptions = isChoiceType(question.type);

  function updateField(field, value) {
    onChange({ ...question, [field]: value });
  }

  function handleTypeChange(newType) {
    let options = question.options;
    // A choice question needs at least 2 options, so add empty boxes to fill in.
    if (isChoiceType(newType)) {
      while (options.length < LIMITS.OPTIONS_MIN) {
        options = [...options, ''];
      }
    }
    onChange({ ...question, type: newType, options });
  }

  function updateOption(optionIndex, value) {
    const options = question.options.map((option, i) => (i === optionIndex ? value : option));
    updateField('options', options);
  }

  function addOption() {
    updateField('options', [...question.options, '']);
  }

  function removeOption(optionIndex) {
    updateField(
      'options',
      question.options.filter((_, i) => i !== optionIndex),
    );
  }

  return (
    <div className="card question-editor">
      <div className="question-editor-header">
        <h3>Question {number}</h3>
        <div className="button-row">
          <button type="button" className="btn btn-small btn-secondary" onClick={onMoveUp} disabled={index === 0}>
            ↑ Up
          </button>
          <button
            type="button"
            className="btn btn-small btn-secondary"
            onClick={onMoveDown}
            disabled={index === total - 1}
          >
            ↓ Down
          </button>
          <button type="button" className="btn btn-small btn-danger" onClick={onRemove}>
            Remove
          </button>
        </div>
      </div>

      <div className="form-group">
        <label htmlFor={`question-${question.key}-text`}>Question text</label>
        <input
          id={`question-${question.key}-text`}
          type="text"
          value={question.text}
          maxLength={LIMITS.QUESTION_TEXT_MAX}
          placeholder="e.g. How was the food?"
          onChange={(e) => updateField('text', e.target.value)}
        />
        {errors.text && <p className="field-error">{errors.text}</p>}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor={`question-${question.key}-type`}>Answer type</label>
          <select
            id={`question-${question.key}-type`}
            value={question.type}
            onChange={(e) => handleTypeChange(e.target.value)}
          >
            {Object.values(QUESTION_TYPES).map((type) => (
              <option key={type} value={type}>
                {QUESTION_TYPE_LABELS[type]}
              </option>
            ))}
          </select>
          {errors.type && <p className="field-error">{errors.type}</p>}
        </div>

        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={question.required}
            onChange={(e) => updateField('required', e.target.checked)}
          />
          Required
        </label>
      </div>

      {showOptions && (
        <div className="form-group">
          <span className="label">Options</span>
          {question.options.map((option, optionIndex) => (
            <div className="option-row" key={optionIndex}>
              <input
                type="text"
                value={option}
                maxLength={LIMITS.OPTION_TEXT_MAX}
                placeholder={`Option ${optionIndex + 1}`}
                aria-label={`Question ${number} option ${optionIndex + 1}`}
                onChange={(e) => updateOption(optionIndex, e.target.value)}
              />
              <button
                type="button"
                className="btn btn-small btn-secondary"
                onClick={() => removeOption(optionIndex)}
                disabled={question.options.length <= LIMITS.OPTIONS_MIN}
                aria-label={`Remove option ${optionIndex + 1}`}
              >
                ×
              </button>
            </div>
          ))}
          <button
            type="button"
            className="btn btn-small btn-secondary"
            onClick={addOption}
            disabled={question.options.length >= LIMITS.OPTIONS_MAX}
          >
            + Add option
          </button>
          {errors.options && <p className="field-error">{errors.options}</p>}
        </div>
      )}
    </div>
  );
}

export default QuestionEditor;
