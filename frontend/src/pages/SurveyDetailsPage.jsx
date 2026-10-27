import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import SuccessMessage from '../components/SuccessMessage';
import useFlashMessage from '../hooks/useFlashMessage';
import { deleteSurvey, getSurvey } from '../services/surveyService';
import { QUESTION_TYPE_LABELS, isChoiceType } from '../utils/constants';
import { formatDateTime, pluralize } from '../utils/format';

// Shows one survey with all its questions and action buttons.
function SurveyDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [survey, setSurvey] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [successMessage, setSuccessMessage] = useFlashMessage();

  async function loadSurvey() {
    setLoading(true);
    setError('');
    try {
      setSurvey(await getSurvey(id));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // Load the survey when the page opens (and again if the id in the URL changes)
  useEffect(() => {
    loadSurvey();
  }, [id]);

  async function handleDelete() {
    const confirmed = window.confirm(
      `Delete "${survey.title}"? All of its responses will also be deleted. This cannot be undone.`,
    );
    if (!confirmed) {
      return;
    }
    setDeleting(true);
    setDeleteError('');
    try {
      await deleteSurvey(id);
      navigate('/', { state: { message: 'Survey deleted successfully.' } });
    } catch (err) {
      setDeleteError(err.message);
      setDeleting(false);
    }
  }

  if (loading) {
    return <Loading message="Loading survey..." />;
  }
  if (error) {
    return (
      <div className="narrow">
        <ErrorMessage message={error} onRetry={loadSurvey} />
        <Link to="/">Back to surveys</Link>
      </div>
    );
  }

  return (
    <div className="narrow">
      <SuccessMessage message={successMessage} onClose={() => setSuccessMessage('')} />
      <ErrorMessage message={deleteError} />

      <div className="card">
        <h1>{survey.title}</h1>
        {survey.description && <p className="survey-description">{survey.description}</p>}
        <div className="survey-card-stats">
          <span className="badge">{pluralize(survey.questions.length, 'question')}</span>
          <span className="badge badge-blue">{pluralize(survey.responseCount, 'response')}</span>
        </div>
        <p className="muted small">Created {formatDateTime(survey.createdAt)}</p>

        <div className="button-row">
          <Link to={`/surveys/${id}/take`} className="btn btn-primary">
            Take survey
          </Link>
          <Link to={`/surveys/${id}/edit`} className="btn btn-secondary">
            Edit
          </Link>
          <Link to={`/surveys/${id}/summary`} className="btn btn-secondary">
            View responses
          </Link>
          <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={deleting}>
            {deleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>

      <h2 className="section-title">Questions</h2>
      <div className="card">
        <ol className="question-list">
          {survey.questions.map((question) => (
            <li key={question.id}>
              <strong>{question.text}</strong>
              <div className="muted small">
                {QUESTION_TYPE_LABELS[question.type]} · {question.required ? 'Required' : 'Optional'}
              </div>
              {isChoiceType(question.type) && (
                <ul className="option-list">
                  {question.options.map((option) => (
                    <li key={option.id}>{option.text}</li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ol>
      </div>

      <Link to="/">← Back to surveys</Link>
    </div>
  );
}

export default SurveyDetailsPage;
