// One small function for each backend endpoint (see API_REFERENCE.md).
import { request } from './api';

// GET /api/surveys?search=... -> list of survey summaries (newest first)
export function getSurveys(search = '') {
  const trimmed = search.trim();
  const query = trimmed ? `?search=${encodeURIComponent(trimmed)}` : '';
  return request(`/api/surveys${query}`);
}

// GET /api/surveys/{id} -> full survey with questions
export function getSurvey(id) {
  return request(`/api/surveys/${id}`);
}

// POST /api/surveys -> created survey
export function createSurvey(surveyRequest) {
  return request('/api/surveys', { method: 'POST', body: surveyRequest });
}

// PUT /api/surveys/{id} -> updated survey
export function updateSurvey(id, surveyRequest) {
  return request(`/api/surveys/${id}`, { method: 'PUT', body: surveyRequest });
}

// DELETE /api/surveys/{id} -> nothing (204)
export function deleteSurvey(id) {
  return request(`/api/surveys/${id}`, { method: 'DELETE' });
}

// POST /api/surveys/{id}/responses -> { responseId, message }
export function submitResponse(surveyId, submitRequest) {
  return request(`/api/surveys/${surveyId}/responses`, { method: 'POST', body: submitRequest });
}

// GET /api/surveys/{id}/responses -> list of individual responses (newest first)
export function getResponses(surveyId) {
  return request(`/api/surveys/${surveyId}/responses`);
}

// GET /api/surveys/{id}/summary -> counts and percentages per question
export function getSummary(surveyId) {
  return request(`/api/surveys/${surveyId}/summary`);
}
