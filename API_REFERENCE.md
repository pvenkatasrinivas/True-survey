# True Survey — API reference

Backend base URL: `http://localhost:8081` (frontend reads it from `VITE_API_BASE_URL`).
Swagger UI: `http://localhost:8081/swagger-ui.html`. No login — this is a simple open survey tool.
JSON everywhere. Dates are ISO-8601 strings.

## Error format (every error)
```json
{
  "timestamp": "2026-10-27T05:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Please answer all required questions",
  "path": "/api/surveys/3/responses",
  "fieldErrors": { "answers.12": "This question is required" }
}
```
`fieldErrors` only appears on 400 validation errors. For survey create/update the keys look like
`title`, `questions`, `questions[0].text`, `questions[1].options`. For response submission the keys are
`answers.<questionId>` so the UI can show the error under the right question.
Other codes: 404 (`"Survey not found with id 99"`), 500.

## Question types
`SHORT_TEXT` (free text, max 1000 chars), `SINGLE_CHOICE` (pick exactly one option), `MULTIPLE_CHOICE` (pick one or more).
Choice questions need 2–10 options. Every survey needs 1–50 questions.

## Surveys
Survey (full):
```json
{
  "id": 3,
  "title": "Canteen Feedback",
  "description": "Tell us what you think about the canteen",
  "createdAt": "2026-10-20T08:15:00Z",
  "updatedAt": "2026-10-20T08:15:00Z",
  "responseCount": 4,
  "questionsLocked": true,
  "questions": [
    { "id": 10, "text": "Your name", "type": "SHORT_TEXT", "required": false, "position": 1, "options": [] },
    { "id": 11, "text": "How was the food?", "type": "SINGLE_CHOICE", "required": true, "position": 2,
      "options": [ { "id": 21, "text": "Good" }, { "id": 22, "text": "Average" }, { "id": 23, "text": "Poor" } ] },
    { "id": 12, "text": "Which meals do you eat here?", "type": "MULTIPLE_CHOICE", "required": true, "position": 3,
      "options": [ { "id": 24, "text": "Breakfast" }, { "id": 25, "text": "Lunch" }, { "id": 26, "text": "Dinner" } ] }
  ]
}
```
`questionsLocked` is `true` once the survey has at least one response. Then the questions can no longer be
changed (so old answers stay correct); only the title and description can be edited.

SurveySummary (list item):
```json
{ "id": 3, "title": "Canteen Feedback", "description": "...", "questionCount": 3, "responseCount": 4, "createdAt": "..." }
```

SurveyRequest (create and update):
```json
{
  "title": "Canteen Feedback",
  "description": "Tell us what you think about the canteen",
  "questions": [
    { "text": "Your name", "type": "SHORT_TEXT", "required": false, "options": [] },
    { "text": "How was the food?", "type": "SINGLE_CHOICE", "required": true, "options": ["Good", "Average", "Poor"] }
  ]
}
```
Validation: `title` 3–150 chars; `description` ≤ 1000; 1–50 questions; question `text` 3–300 chars; `type` required;
choice questions 2–10 non-empty, unique options (≤ 150 chars each); `SHORT_TEXT` must have no options.
Question order = array order.

| Method | Path | Body | Success |
|---|---|---|---|
| GET | `/api/surveys?search=canteen` | — (`search` optional, matches title) | `200` `[SurveySummary]` newest first |
| GET | `/api/surveys/{id}` | — | `200` Survey / `404` |
| POST | `/api/surveys` | SurveyRequest | `201` Survey |
| PUT | `/api/surveys/{id}` | SurveyRequest | `200` Survey. If the survey already has responses, only title/description are updated and `questions` is ignored. |
| DELETE | `/api/surveys/{id}` | — | `204` (its responses are deleted too) |

## Responses
| Method | Path | Body | Success |
|---|---|---|---|
| POST | `/api/surveys/{id}/responses` | SubmitRequest | `201 { "responseId": 41, "message": "Thank you! Your response has been submitted." }` |
| GET | `/api/surveys/{id}/responses` | — | `200` `[ResponseView]` newest first |
| GET | `/api/surveys/{id}/summary` | — | `200` Summary |

SubmitRequest:
```json
{ "answers": [
    { "questionId": 10, "text": "Ravi" },
    { "questionId": 11, "optionIds": [21] },
    { "questionId": 12, "optionIds": [24, 25] }
] }
```
Rules (checked on the server): every required question must be answered; `SHORT_TEXT` uses `text` (trimmed, ≤ 1000);
`SINGLE_CHOICE` exactly one `optionIds`; `MULTIPLE_CHOICE` one or more; option ids must belong to that question;
unknown question ids are rejected. Optional questions can be left out.

ResponseView:
```json
{ "id": 41, "submittedAt": "2026-10-27T05:31:02Z",
  "answers": [ { "questionId": 11, "questionText": "How was the food?", "answer": "Good" },
               { "questionId": 12, "questionText": "Which meals do you eat here?", "answer": "Breakfast, Lunch" } ] }
```
Only answered questions are listed.

Summary:
```json
{
  "surveyId": 3, "title": "Canteen Feedback", "totalResponses": 4,
  "questions": [
    { "questionId": 11, "text": "How was the food?", "type": "SINGLE_CHOICE", "answeredCount": 4,
      "options": [ { "optionId": 21, "text": "Good", "count": 3, "percentage": 75.0 },
                   { "optionId": 22, "text": "Average", "count": 1, "percentage": 25.0 },
                   { "optionId": 23, "text": "Poor", "count": 0, "percentage": 0.0 } ],
      "textAnswers": [] },
    { "questionId": 10, "text": "Your name", "type": "SHORT_TEXT", "answeredCount": 2,
      "options": [], "textAnswers": [ "Ravi", "Priya" ] }
  ]
}
```
`percentage` = count ÷ answeredCount × 100, rounded to 1 decimal (0 when nobody answered). For multiple choice the
percentages can add up to more than 100 because one person can pick several options. `textAnswers` shows the latest 20.
