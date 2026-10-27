import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import SurveyForm, { surveyToFormValues } from '../components/SurveyForm';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import { getSurvey, updateSurvey } from '../services/surveyService';

// Loads a survey and shows it in the form for editing.
function EditSurveyPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [survey, setSurvey] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  async function handleUpdate(surveyRequest) {
    await updateSurvey(id, surveyRequest);
    navigate(`/surveys/${id}`, { state: { message: 'Survey updated successfully.' } });
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
      <h1>Edit Survey</h1>
      <SurveyForm
        initialValues={surveyToFormValues(survey)}
        questionsLocked={survey.questionsLocked}
        submitLabel="Save changes"
        onSubmit={handleUpdate}
        onCancel={() => navigate(`/surveys/${id}`)}
      />
    </div>
  );
}

export default EditSurveyPage;
