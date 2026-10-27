import { useNavigate } from 'react-router-dom';
import SurveyForm from '../components/SurveyForm';
import { createSurvey } from '../services/surveyService';

// Page with an empty survey form. After saving it opens the new survey's details page.
function CreateSurveyPage() {
  const navigate = useNavigate();

  async function handleCreate(surveyRequest) {
    // If this throws, SurveyForm catches it and shows the error.
    const created = await createSurvey(surveyRequest);
    navigate(`/surveys/${created.id}`, { state: { message: 'Survey created successfully.' } });
  }

  return (
    <div className="narrow">
      <h1>Create Survey</h1>
      <SurveyForm submitLabel="Create survey" onSubmit={handleCreate} onCancel={() => navigate('/')} />
    </div>
  );
}

export default CreateSurveyPage;
