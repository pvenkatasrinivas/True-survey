import { Link } from 'react-router-dom';
import { formatDateTime, pluralize } from '../utils/format';

// One survey in the list on the Home page.
function SurveyCard({ survey }) {
  return (
    <article className="card survey-card">
      <h3 className="survey-card-title">
        <Link to={`/surveys/${survey.id}`}>{survey.title}</Link>
      </h3>
      {survey.description && <p className="survey-card-description">{survey.description}</p>}

      <div className="survey-card-stats">
        <span className="badge">{pluralize(survey.questionCount, 'question')}</span>
        <span className="badge badge-blue">{pluralize(survey.responseCount, 'response')}</span>
      </div>
      <p className="muted small">Created {formatDateTime(survey.createdAt)}</p>

      <div className="button-row">
        <Link to={`/surveys/${survey.id}/take`} className="btn btn-primary btn-small">
          Take Survey
        </Link>
        <Link to={`/surveys/${survey.id}`} className="btn btn-secondary btn-small">
          Details
        </Link>
        <Link to={`/surveys/${survey.id}/edit`} className="btn btn-secondary btn-small">
          Edit
        </Link>
        <Link to={`/surveys/${survey.id}/summary`} className="btn btn-secondary btn-small">
          Responses
        </Link>
      </div>
    </article>
  );
}

export default SurveyCard;
