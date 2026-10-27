import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import SurveyCard from '../components/SurveyCard';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';
import SuccessMessage from '../components/SuccessMessage';
import useFlashMessage from '../hooks/useFlashMessage';
import { getSurveys } from '../services/surveyService';

// Home page: list of all surveys with a search box.
function HomePage() {
  const [surveys, setSurveys] = useState([]);
  const [searchText, setSearchText] = useState('');
  const [searchedFor, setSearchedFor] = useState(''); // the search the current list belongs to
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useFlashMessage();

  async function loadSurveys(search) {
    setLoading(true);
    setError('');
    try {
      const data = await getSurveys(search);
      setSurveys(data || []);
      setSearchedFor(search.trim());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  // Load all surveys when the page opens
  useEffect(() => {
    loadSurveys('');
  }, []);

  function handleSearch(event) {
    event.preventDefault();
    loadSurveys(searchText);
  }

  function clearSearch() {
    setSearchText('');
    loadSurveys('');
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Surveys</h1>
          <p className="muted">Create surveys, collect answers and see the results.</p>
        </div>
        <Link to="/surveys/new" className="btn btn-primary">
          + Create Survey
        </Link>
      </div>

      <SuccessMessage message={successMessage} onClose={() => setSuccessMessage('')} />

      <form className="search-bar" onSubmit={handleSearch} role="search">
        <input
          type="search"
          value={searchText}
          placeholder="Search surveys by title"
          aria-label="Search surveys by title"
          onChange={(e) => setSearchText(e.target.value)}
        />
        <button type="submit" className="btn btn-secondary">
          Search
        </button>
        {searchedFor && (
          <button type="button" className="btn btn-secondary" onClick={clearSearch}>
            Clear
          </button>
        )}
      </form>

      {loading && <Loading message="Loading surveys..." />}

      {!loading && error && <ErrorMessage message={error} onRetry={() => loadSurveys(searchText)} />}

      {!loading && !error && surveys.length === 0 && searchedFor && (
        <EmptyState title="No surveys found" message={`No survey title matches "${searchedFor}".`}>
          <button type="button" className="btn btn-secondary" onClick={clearSearch}>
            Show all surveys
          </button>
        </EmptyState>
      )}

      {!loading && !error && surveys.length === 0 && !searchedFor && (
        <EmptyState title="No surveys yet" message="Surveys you create will appear here.">
          <Link to="/surveys/new" className="btn btn-primary">
            Create your first survey
          </Link>
        </EmptyState>
      )}

      {!loading && !error && surveys.length > 0 && (
        <div className="survey-grid">
          {surveys.map((survey) => (
            <SurveyCard key={survey.id} survey={survey} />
          ))}
        </div>
      )}
    </div>
  );
}

export default HomePage;
