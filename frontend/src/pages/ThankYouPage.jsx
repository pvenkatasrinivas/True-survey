import { Link, useLocation, useParams } from 'react-router-dom';

// Shown after a response is submitted.
function ThankYouPage() {
  const { id } = useParams();
  const location = useLocation();
  // The Take Survey page passes the server message and survey title (they are missing after a refresh).
  const message = location.state?.message || 'Thank you! Your response has been submitted.';
  const surveyTitle = location.state?.title;

  return (
    <div className="narrow">
      <div className="card thank-you">
        <div className="thank-you-icon" aria-hidden="true">
          ✓
        </div>
        <h1>{message}</h1>
        {surveyTitle && <p className="muted">Survey: {surveyTitle}</p>}
        <div className="button-row center">
          <Link to="/" className="btn btn-primary">
            Back to surveys
          </Link>
          <Link to={`/surveys/${id}/summary`} className="btn btn-secondary">
            View summary
          </Link>
        </div>
      </div>
    </div>
  );
}

export default ThankYouPage;
