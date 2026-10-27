// Small helper around fetch() that every service function uses.
// It adds the base URL, sends/reads JSON and turns server errors into normal JS errors.

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

export const NETWORK_ERROR_MESSAGE =
  'Cannot connect to the server. Please check that the backend is running and try again.';

/**
 * Calls the backend and returns the parsed JSON body (or null when there is no body, e.g. 204).
 * On failure it throws an Error with:
 *   error.status      - HTTP status code (0 when the server could not be reached)
 *   error.message     - message from the server, or a friendly default
 *   error.fieldErrors - object like { "title": "Title is required" } (empty object if none)
 */
export async function request(path, options = {}) {
  const { method = 'GET', body } = options;

  const fetchOptions = { method, headers: { Accept: 'application/json' } };
  if (body !== undefined) {
    fetchOptions.headers['Content-Type'] = 'application/json';
    fetchOptions.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(API_BASE_URL + path, fetchOptions);
  } catch {
    // fetch() only throws when the request never reached the server (server down, no internet, CORS).
    const error = new Error(NETWORK_ERROR_MESSAGE);
    error.status = 0;
    error.fieldErrors = {};
    throw error;
  }

  const data = await readJson(response);

  if (!response.ok) {
    const message = (data && data.message) || `Request failed (status ${response.status})`;
    const error = new Error(message);
    error.status = response.status;
    error.fieldErrors = (data && data.fieldErrors) || {};
    throw error;
  }

  return data;
}

// Reads the body as JSON. Returns null for an empty body or a body that is not JSON.
async function readJson(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}
