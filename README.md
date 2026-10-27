# True Survey — Survey and Data Collection System

True Survey lets you create a survey with a few questions, share it, collect answers, and see a simple summary
of the results (how many responses, how many people picked each option, and the text answers).

The code is organised in clear layers with commented, readable classes. There is no login: anyone who opens the
app can create surveys and answer them.

---

## Features

| Area | What it does |
|------|--------------|
| Survey management | List (with search), create, view details, edit, delete surveys |
| Questions | Short text, single choice (radio buttons), multiple choice (checkboxes); required or optional; reorder while editing |
| Participation | Open a survey, answer, required answers are checked, submit, see a thank-you message |
| Responses | List every submitted response with its answers and time |
| Summary | Total responses; per question: answered count, count + percentage per option, latest text answers |
| Data safety | Once a survey has responses its questions are locked (only title/description can change), so old answers always match their questions |
| API docs | Swagger UI generated from the code + a Postman collection |
| UI | Responsive (mobile, tablet, desktop), loading/empty/error states, success messages |

## Technology stack

| Layer | Technology |
|-------|-----------|
| Backend | Java (8-compatible code), Spring Boot 2.7.18, Spring MVC (REST), Spring Data JPA, Hibernate ORM 5.6, Bean Validation |
| API docs | springdoc-openapi 1.7.0 (Swagger UI / OpenAPI 3) |
| Database | PostgreSQL (scripts tested on 16; 12+ works) |
| Frontend | React 18, React Router 6, JavaScript, plain CSS, Vite 5 |
| Build | Maven (backend), npm (frontend) |
| Testing | JUnit 5, MockMvc, H2 in-memory DB (backend); Vitest + React Testing Library (frontend); Postman |

Spring Boot 2.7 is used because it supports Java 8 through 21 (Spring Boot 3 needs Java 17+).

## Project structure

```
True-Survey/
├── backend/                               Spring Boot REST API
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/truesurvey/
│       │   ├── TrueSurveyApplication.java
│       │   ├── entity/       Survey, Question, QuestionOption, SurveyResponse, Answer, QuestionType
│       │   ├── repository/   SurveyRepository, SurveyResponseRepository, AnswerRepository
│       │   ├── service/      SurveyService (CRUD + rules), ResponseService (submit, list, summary)
│       │   ├── controller/   SurveyController, ResponseController
│       │   ├── dto/          request / response objects
│       │   ├── config/       OpenApiConfig (Swagger), WebConfig (CORS)
│       │   └── exception/    custom exceptions + GlobalExceptionHandler
│       ├── main/resources/application.properties
│       └── test/             JUnit tests (+ application-test.properties for H2)
├── frontend/                              React app (Vite)
│   └── src/
│       ├── pages/        Home, CreateSurvey, EditSurvey, SurveyDetails, TakeSurvey, ThankYou, ResponseSummary, NotFound
│       ├── components/   Navbar, SurveyCard, SurveyForm, QuestionEditor, QuestionField, messages
│       ├── services/     api.js (fetch helper), surveyService.js
│       ├── utils/        validation.js, constants.js, format.js
│       ├── styles/       global.css
│       └── __tests__/    Vitest tests
├── database/             create-database.sql, schema.sql, sample-data.sql
├── postman/              True-Survey.postman_collection.json
├── API_REFERENCE.md      every endpoint with request/response examples
└── .env.example
```

## Prerequisites

| Tool | Version | Check with |
|------|---------|-----------|
| JDK | 8, 11, 17 or 21 | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| PostgreSQL | 12+ | `psql --version` |
| Node.js | 18+ (20 or 22 recommended) | `node -v` |
| Postman | any (optional) | — |

---

## 1. PostgreSQL setup

1. Install PostgreSQL and remember the `postgres` user's password.
2. Create the database:
   ```bash
   psql -U postgres -f database/create-database.sql
   # or: psql -U postgres -c "CREATE DATABASE truesurvey;"
   ```
3. **No need to create tables** — Hibernate creates them on the first start (`ddl-auto=update`).
   `database/schema.sql` shows the same tables and relationships in SQL.
4. Optional, after the backend has started once — load a sample survey with 2 responses:
   ```bash
   psql -U postgres -d truesurvey -f database/sample-data.sql
   ```

## 2. Configuration

Settings come from environment variables (see `backend/src/main/resources/application.properties`).

| Variable | Required | Default | Meaning |
|----------|----------|---------|---------|
| `DB_PASSWORD` | **yes** | — | Password of your PostgreSQL user |
| `DB_URL` | no | `jdbc:postgresql://localhost:5432/truesurvey` | Database address |
| `DB_USERNAME` | no | `postgres` | Database user |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5174` | Frontend URL(s), comma-separated |
| `PORT` | no | `8081` | Backend port |

macOS / Linux: `export DB_PASSWORD='your_postgres_password'`
Windows PowerShell: `$env:DB_PASSWORD="your_postgres_password"`
IntelliJ: *Run → Edit Configurations → Environment variables* → `DB_PASSWORD=...`

Frontend: copy `frontend/.env.example` to `frontend/.env` only if the backend is not on `http://localhost:8081`.

(True Survey uses port **8081** / **5174** so it can run next to True Track on 8080 / 5173.)

## 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```
Starts on **http://localhost:8081**. Or build a jar: `mvn clean package` → `java -jar target/true-survey-backend.jar`.

## 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```
Open **http://localhost:5174**. Production build: `npm run build` (output in `frontend/dist/`).

## API documentation

- **Swagger UI:** http://localhost:8081/swagger-ui.html (try every endpoint from the browser)
- **OpenAPI JSON:** http://localhost:8081/v3/api-docs
- **Full examples:** [API_REFERENCE.md](API_REFERENCE.md)
- **Postman:** import `postman/True-Survey.postman_collection.json`

| Method | URL | Description | Success |
|--------|-----|-------------|---------|
| GET | `/api/surveys?search=` | List surveys (newest first), optional title search | 200 |
| POST | `/api/surveys` | Create a survey with its questions | 201 / 400 |
| GET | `/api/surveys/{id}` | One survey with questions and options | 200 / 404 |
| PUT | `/api/surveys/{id}` | Update survey (questions only if no responses yet) | 200 / 400 / 404 |
| DELETE | `/api/surveys/{id}` | Delete survey and its responses | 204 / 404 |
| POST | `/api/surveys/{id}/responses` | Submit answers | 201 / 400 / 404 |
| GET | `/api/surveys/{id}/responses` | All responses with answers | 200 / 404 |
| GET | `/api/surveys/{id}/summary` | Counts, percentages, text answers | 200 / 404 |

Create a survey:
```json
POST /api/surveys
{
  "title": "Canteen Feedback",
  "description": "Tell us what you think about the canteen",
  "questions": [
    { "text": "Your name", "type": "SHORT_TEXT", "required": false, "options": [] },
    { "text": "How was the food?", "type": "SINGLE_CHOICE", "required": true, "options": ["Good", "Average", "Poor"] },
    { "text": "Which meals do you eat here?", "type": "MULTIPLE_CHOICE", "required": true, "options": ["Breakfast", "Lunch", "Dinner"] }
  ]
}
```
Submit a response (use the question and option **ids** returned when the survey was created):
```json
POST /api/surveys/1/responses
{ "answers": [
    { "questionId": 1, "text": "Ravi" },
    { "questionId": 2, "optionIds": [4] },
    { "questionId": 3, "optionIds": [7, 8] }
] }
```
Missing a required answer:
```json
400 Bad Request
{ "status": 400, "message": "Please fix the highlighted answers",
  "fieldErrors": { "answers.2": "This question is required" } }
```

**Validation rules**: title 3–150 chars; 1–50 questions; question text 3–300 chars; choice questions need 2–10
unique, non-empty options; short-text questions have no options; text answers up to 1000 characters;
single choice = exactly one option; multiple choice = at least one.

**Percentages** in the summary = option count ÷ people who answered that question × 100 (1 decimal).
For multiple choice they can add up to more than 100%, because one person can pick several options.

---

## Testing

**Backend (JUnit)** — runs on an in-memory H2 database, no PostgreSQL needed:
```bash
cd backend
mvn test
```
| Test class | Type | What it checks |
|-----------|------|----------------|
| `SurveyRulesTest` | Unit | percentage rounding / divide-by-zero, option rules |
| `SurveyControllerTest` | Integration (MockMvc + H2) | create + read with ordered questions, list + search, update, delete, field errors, option rules, bad type, invalid/unknown ids, Swagger `/v3/api-docs` loads |
| `ResponseControllerTest` | Integration (MockMvc + H2) | valid submit, optional skip, required missing, wrong/foreign options, unknown question, 404 survey, summary counts and percentages, empty summary, questions locked after responses, delete removes responses |

**Frontend (Vitest)**:
```bash
cd frontend
npm test        # 35 tests: validation rules, api helper, QuestionField, SurveyForm
npm run build
```

**Postman**: import the collection, start the backend, *Run collection*. *Create survey* stores the survey,
question and option ids in collection variables, so the submit/summary requests work without editing.

**Manual check list**: create a survey with all 3 question types → it shows on the home page → take it and
try submitting without required answers → submit properly → thank-you page → summary shows 1 response and
correct counts → edit (questions now read-only) → delete.

### Verification status of this delivery
- Frontend: `npm test` (35 tests) and `npm run build` were run and passed.
- Database: `database/*.sql` were run on PostgreSQL 16 without errors, including the summary math and the
  delete order as plain SQL.
- Postman collection: JSON structure and all test scripts were validated with the Postman collection SDK.
- Backend: the code was written and reviewed, but it was **not compiled or run** in the build environment
  (Maven Central was not reachable there). Run `mvn test` first on your machine; fix anything it reports
  before relying on it.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Could not resolve placeholder 'DB_PASSWORD'` | Set the `DB_PASSWORD` environment variable in the window/IDE that starts the backend. |
| `password authentication failed` | Wrong `DB_PASSWORD` / `DB_USERNAME`. |
| `database "truesurvey" does not exist` | Run step 1 of *PostgreSQL setup*. |
| `Connection to localhost:5432 refused` | Start PostgreSQL. |
| `Port 8081 was already in use` | Stop the other program or use another `PORT` and update `VITE_API_BASE_URL` in `frontend/.env`. |
| CORS error in the browser console | Add the frontend URL to `CORS_ALLOWED_ORIGINS` (default `http://localhost:5174`). |
| "Cannot connect to the server" in the UI | Backend not running or `VITE_API_BASE_URL` wrong; restart `npm run dev` after changing `.env`. |
| Swagger page is blank / 404 | Use `http://localhost:8081/swagger-ui.html` (it redirects to `/swagger-ui/index.html`). |
| Can't edit questions | The survey already has responses. Create a new survey (or delete the responses by deleting the survey). |
| `npm audit` warnings | They are in dev tools (Vite/Vitest/React Router 6) and don't affect local use; upgrading to Vite 6+/React Router 7 fixes them. |
