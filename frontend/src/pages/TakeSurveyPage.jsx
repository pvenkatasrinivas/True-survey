import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import QuestionField from '../components/QuestionField';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import { getSurvey, submitResponse } from '../services/surveyService';
import { buildSubmitPayload, validateAnswers } from '../utils/validation';

// Lets a user answer a survey and submit the response.
function TakeSurveyPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [survey, setSurvey] = useState(null);
  const [answers, setAnswers] = useState({}); // { [questionId]: string or array of option ids }
  const [errors, setErrors] = useState({}); // { "answers.<questionId>": "message" }
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function loadSurvey() {
    setLoading(true);
    setLoadError('');
    try {
      setSurvey(await getSurvey(id));
    } catch (err) {
      setLoadError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // Load the survey when the page opens (and again if the id in the URL changes)
  useEffect(() => {
    loadSurvey();
  }, [id]);

  function handleAnswerChange(questionId, value) {
    setAnswers({ ...answers, [questionId]: value });
    // Remove the error under this question as soon as the user changes the answer
    const key = `answers.${questionId}`;
    if (errors[key]) {
      const remainingErrors = { ...errors };
      delete remainingErrors[key];
      setErrors(remainingErrors);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError('');

    const validationErrors = validateAnswers(survey.questions, answers);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      setSubmitError('Please answer all required questions correctly.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await submitResponse(id, buildSubmitPayload(survey.questions, answers));
      navigate(`/surveys/${id}/thank-you`, { state: { message: result?.message, title: survey.title } });
    } catch (err) {
      // Server errors use the same "answers.<questionId>" keys, so they show under each question.
      setSubmitError(err.message);
      setErrors(err.fieldErrors || {});
      setSubmitting(false);
    }
  }

  if (loading) {
    return <Loading message="Loading survey..." />;
  }
  if (loadError) {
    return (
      <div className="narrow">
        <ErrorMessage message={loadError} onRetry={loadSurvey} />
        <Link to="/">Back to surveys</Link>
      </div>
    );
  }

  return (
    <div className="narrow">
      <div className="card">
        <h1>{survey.title}</h1>
        {survey.description && <p className="survey-description">{survey.description}</p>}
        <p className="muted small">
          Questions marked with <span className="required-mark">*</span> are required.
        </p>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        {survey.questions.map((question, index) => (
          <QuestionField
            key={question.id}
            question={question}
            number={index + 1}
            value={answers[question.id]}
            error={errors[`answers.${question.id}`]}
            onChange={(value) => handleAnswerChange(question.id, value)}
          />
        ))}

        <ErrorMessage message={submitError} />

        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit response'}
          </button>
          <Link to="/" className="btn btn-secondary">
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );
}

export default TakeSurveyPage;
