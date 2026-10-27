import { afterEach, describe, expect, it, vi } from 'vitest';
import { API_BASE_URL, NETWORK_ERROR_MESSAGE, request } from '../services/api';

// Creates a fake fetch Response with a status and a JSON (or empty) body.
function fakeResponse(status, body) {
  const text = body === undefined ? '' : JSON.stringify(body);
  return {
    ok: status >= 200 && status < 300,
    status,
    text: () => Promise.resolve(text),
  };
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('request()', () => {
  it('returns parsed JSON on success and calls the right URL', async () => {
    const fetchMock = vi.fn().mockResolvedValue(fakeResponse(200, [{ id: 1, title: 'A' }]));
    vi.stubGlobal('fetch', fetchMock);

    const data = await request('/api/surveys');

    expect(data).toEqual([{ id: 1, title: 'A' }]);
    expect(fetchMock).toHaveBeenCalledWith(`${API_BASE_URL}/api/surveys`, expect.objectContaining({ method: 'GET' }));
  });

  it('sends a JSON body for POST', async () => {
    const fetchMock = vi.fn().mockResolvedValue(fakeResponse(201, { id: 5 }));
    vi.stubGlobal('fetch', fetchMock);

    await request('/api/surveys', { method: 'POST', body: { title: 'Test' } });

    const options = fetchMock.mock.calls[0][1];
    expect(options.method).toBe('POST');
    expect(options.headers['Content-Type']).toBe('application/json');
    expect(options.body).toBe('{"title":"Test"}');
  });

  it('returns null for an empty 204 response', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(fakeResponse(204)));
    await expect(request('/api/surveys/1', { method: 'DELETE' })).resolves.toBeNull();
  });

  it('throws an error with status, message and fieldErrors for a 400', async () => {
    const body = {
      status: 400,
      message: 'Please answer all required questions',
      fieldErrors: { 'answers.12': 'This question is required' },
    };
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(fakeResponse(400, body)));

    const error = await request('/api/surveys/3/responses', { method: 'POST', body: {} }).catch((e) => e);

    expect(error).toBeInstanceOf(Error);
    expect(error.status).toBe(400);
    expect(error.message).toBe('Please answer all required questions');
    expect(error.fieldErrors).toEqual({ 'answers.12': 'This question is required' });
  });

  it('uses the server message for 404 and an empty fieldErrors object', async () => {
    const body = { status: 404, message: 'Survey not found with id 99' };
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(fakeResponse(404, body)));

    const error = await request('/api/surveys/99').catch((e) => e);

    expect(error.status).toBe(404);
    expect(error.message).toBe('Survey not found with id 99');
    expect(error.fieldErrors).toEqual({});
  });

  it('uses a default message when the error body is empty', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(fakeResponse(500)));

    const error = await request('/api/surveys').catch((e) => e);

    expect(error.status).toBe(500);
    expect(error.message).toBe('Request failed (status 500)');
  });

  it('shows a friendly message when the server cannot be reached', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    const error = await request('/api/surveys').catch((e) => e);

    expect(error.status).toBe(0);
    expect(error.message).toBe(NETWORK_ERROR_MESSAGE);
    expect(error.message).toMatch(/^Cannot connect to the server/);
  });
});
