import { Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import CreateSurveyPage from './pages/CreateSurveyPage';
import EditSurveyPage from './pages/EditSurveyPage';
import SurveyDetailsPage from './pages/SurveyDetailsPage';
import TakeSurveyPage from './pages/TakeSurveyPage';
import ThankYouPage from './pages/ThankYouPage';
import ResponseSummaryPage from './pages/ResponseSummaryPage';
import NotFoundPage from './pages/NotFoundPage';

// All pages of the app. "/surveys/new" is listed before "/surveys/:id" for readability
// (React Router picks the most specific match anyway).
function App() {
  return (
    <>
      <Navbar />
      <main className="container page">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/surveys/new" element={<CreateSurveyPage />} />
          <Route path="/surveys/:id" element={<SurveyDetailsPage />} />
          <Route path="/surveys/:id/edit" element={<EditSurveyPage />} />
          <Route path="/surveys/:id/take" element={<TakeSurveyPage />} />
          <Route path="/surveys/:id/thank-you" element={<ThankYouPage />} />
          <Route path="/surveys/:id/summary" element={<ResponseSummaryPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </>
  );
}

export default App;
