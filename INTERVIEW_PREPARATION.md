# True Survey — Interview Preparation

Simple notes to help you explain this project. Read them along with the code.

---

## 1. Explain the project in 30 seconds

- True Survey is a small app to create surveys and collect answers.
- You create a survey with questions (short text, single choice, multiple choice) and mark some as required.
- People open the survey, answer, and submit. The answers are saved in PostgreSQL.
- Then you see the responses and a summary: how many people answered and how many picked each option.
- Backend is Spring Boot with Spring MVC and JPA/Hibernate; frontend is React; APIs are documented with Swagger.

## 2. Survey workflow

```
Create survey ──► Survey list ──► Take survey ──► Validate ──► Save response ──► Summary
 (title,           (home page)    (answer each     (required,    (survey_responses  (counts and
  questions,                       question)        option ids)    + answers rows)    percentages)
  options)
```

1. **Create** — `SurveyForm` sends `POST /api/surveys` with the title and a list of questions.
   `SurveyService` checks the rules and saves the survey; questions and options are saved too (cascade).
2. **Take** — `TakeSurveyPage` loads `GET /api/surveys/{id}` and shows each question with `QuestionField`
   (text box, radio buttons or checkboxes).
3. **Validate** — the browser checks required answers first. The server checks again in `ResponseService`
   (required, one option for single choice, options really belong to the question, text length).
4. **Save** — one `SurveyResponse` row + one `Answer` row per text answer or per selected option.
5. **Summary** — `GET /api/surveys/{id}/summary` counts the answers per option and returns percentages.

Rule to explain: **once a survey has responses, its questions are locked.** If someone could delete a question
after people answered it, old answers would point to nothing. So after the first response only the title and
description can change (`questionsLocked: true` in the API).

## 3. Database entities and relationships

```
surveys 1 ──── * questions 1 ──── * question_options
   │                 ▲                   ▲
   1                 │ *                 │ 0..1
   │                 │                   │
   * survey_responses 1 ──── * answers ──┘
```

| Entity (table) | Main fields | Relationship |
|---------------|-------------|--------------|
| `Survey` (surveys) | id, title, description, createdAt, updatedAt | has many Questions (`@OneToMany`, cascade + orphanRemoval) |
| `Question` (questions) | id, text, type, required, position | belongs to one Survey (`@ManyToOne`), has many Options |
| `QuestionOption` (question_options) | id, text, position | belongs to one Question |
| `SurveyResponse` (survey_responses) | id, submittedAt | belongs to one Survey, has many Answers |
| `Answer` (answers) | id, textValue | belongs to one Response, one Question, and (for choice questions) one Option |

**Why one Answer row per selected option?** A multiple-choice answer with 2 options becomes 2 rows. Then
"how many people picked Lunch" is just a count of rows with that option — simple and no comma-separated text.

`@OneToMany(mappedBy = "survey")` means the foreign key (`survey_id`) is in the `questions` table.
`cascade = CascadeType.ALL` saves/deletes the questions together with the survey.
`orphanRemoval = true` deletes a question from the database when it is removed from the list.
`FetchType.LAZY` loads related data only when it's used.

## 4. REST APIs

| Method | URL | What it does | Status |
|--------|-----|--------------|--------|
| GET | `/api/surveys` | list surveys | 200 |
| POST | `/api/surveys` | create | 201 |
| GET | `/api/surveys/{id}` | one survey | 200 / 404 |
| PUT | `/api/surveys/{id}` | update | 200 |
| DELETE | `/api/surveys/{id}` | delete (+ its responses) | 204 |
| POST | `/api/surveys/{id}/responses` | submit answers | 201 / 400 |
| GET | `/api/surveys/{id}/responses` | list responses | 200 |
| GET | `/api/surveys/{id}/summary` | counts and percentages | 200 |

REST = resources (surveys, responses) in the URL, the HTTP method says the action, JSON in and out, and the
status code tells the result. The URLs are nested (`/surveys/{id}/responses`) because a response always belongs
to a survey.

## 5. Spring Boot, JPA, Hibernate and React — simple explanations

**Spring Boot** — starts the app with an embedded Tomcat server and auto-configures things based on the
dependencies in `pom.xml` (web, JPA, validation, PostgreSQL driver).

**Spring MVC** — the web part. `@RestController` + `@GetMapping/@PostMapping` map URLs to methods; Jackson turns
Java objects into JSON. `@PathVariable` reads `{id}` from the URL, `@RequestBody` reads the JSON body.

**JPA** — the Java standard for working with databases using objects (`@Entity`, `@Id`, `@OneToMany` ...).

**Hibernate** — the JPA implementation. It turns `surveyRepository.save(survey)` into `INSERT` statements and
creates the tables from the entities (`ddl-auto=update`).

**Spring Data JPA** — I only write repository interfaces. `findByTitleContainingIgnoreCase(...)` becomes a
`LIKE` query from the method name; `@Query` holds custom JPQL like the GROUP BY count queries.

**Swagger / OpenAPI (springdoc)** — reads my controllers and creates live API docs at `/swagger-ui.html`.
`@Operation` and `@ApiResponse` add descriptions.

**React** — the UI is built from components (`QuestionField`, `SurveyCard` ...). `useState` holds form data,
`useEffect` loads data from the API, React Router switches pages.

## 6. Example: create a survey and submit a response

Create:
```http
POST /api/surveys
Content-Type: application/json

{ "title": "Canteen Feedback", "description": "Quick feedback",
  "questions": [
    { "text": "How was the food?", "type": "SINGLE_CHOICE", "required": true, "options": ["Good", "Average", "Poor"] },
    { "text": "Any suggestions?", "type": "SHORT_TEXT", "required": false, "options": [] } ] }
```
```json
201 Created
{ "id": 1, "title": "Canteen Feedback", "responseCount": 0, "questionsLocked": false,
  "questions": [
    { "id": 1, "text": "How was the food?", "type": "SINGLE_CHOICE", "required": true, "position": 1,
      "options": [ { "id": 1, "text": "Good" }, { "id": 2, "text": "Average" }, { "id": 3, "text": "Poor" } ] },
    { "id": 2, "text": "Any suggestions?", "type": "SHORT_TEXT", "required": false, "position": 2, "options": [] } ] }
```
Submit:
```http
POST /api/surveys/1/responses
Content-Type: application/json

{ "answers": [ { "questionId": 1, "optionIds": [1] }, { "questionId": 2, "text": "More fruit" } ] }
```
```json
201 Created
{ "responseId": 1, "message": "Thank you! Your response has been submitted." }
```
Summary after that one response:
```json
{ "surveyId": 1, "title": "Canteen Feedback", "totalResponses": 1,
  "questions": [
    { "questionId": 1, "type": "SINGLE_CHOICE", "answeredCount": 1,
      "options": [ { "text": "Good", "count": 1, "percentage": 100.0 },
                   { "text": "Average", "count": 0, "percentage": 0.0 },
                   { "text": "Poor", "count": 0, "percentage": 0.0 } ] },
    { "questionId": 2, "type": "SHORT_TEXT", "answeredCount": 1, "textAnswers": ["More fruit"] } ] }
```

---

## 7. Common interview questions (short answers)

**Q1. What is Spring Boot?**
- Spring with auto-configuration and an embedded server.
- Add starters in `pom.xml`, run the main class, and the app is up.

**Q2. What is Spring MVC?**
- The web layer of Spring.
- It sends each HTTP request to the right controller method and turns the result into JSON.

**Q3. `@RestController` vs `@Controller`?**
- `@RestController` returns data (JSON).
- `@Controller` is for returning HTML views.

**Q4. JPA vs Hibernate vs Spring Data JPA?**
- JPA = the standard. Hibernate = the library that does the work.
- Spring Data JPA = ready repositories on top, so I don't write SQL for CRUD.

**Q5. What is `@OneToMany` / `@ManyToOne`?**
- One survey has many questions; each question belongs to one survey.
- `@ManyToOne` side has the foreign key column (`survey_id`).

**Q6. What do cascade and orphanRemoval do?**
- Cascade: saving or deleting the survey also saves or deletes its questions.
- orphanRemoval: removing a question from the list deletes it from the DB.

**Q7. Lazy vs eager loading?**
- Lazy loads related data only when you use it. Eager loads it right away.
- I used LAZY and load what I need inside `@Transactional` service methods.

**Q8. What is the N+1 problem and did you avoid it?**
- 1 query for the list + 1 more query per item = slow.
- For the survey list I count questions and responses with two GROUP BY queries instead of one per survey.
  For responses I use `JOIN FETCH` to load answers in the same query.

**Q9. How do you validate input?**
- Simple rules with annotations: `@NotBlank`, `@Size`, `@NotNull` and `@Valid`.
- Rules that need logic (2–10 unique options, required answers) are in the service and throw `ValidationException`.

**Q10. Why validate on the server if React already validates?**
- Anyone can call the API directly with Postman or a script, skipping the UI.
- The server is the real safety check.

**Q11. How do you handle errors?**
- Services throw `ResourceNotFoundException` or `ValidationException`.
- `@RestControllerAdvice` turns them into JSON with 404 or 400 and a clear message.

**Q12. Why do you return DTOs instead of entities?**
- I control the JSON shape, avoid lazy-loading problems and endless loops (survey → question → survey).

**Q13. How is the summary calculated?**
- Load all answers of the survey, then count in Java how many picked each option.
- Percentage = count ÷ people who answered that question × 100. If nobody answered, it's 0 (no divide by zero).

**Q14. Why can multiple-choice percentages be more than 100% in total?**
- One person can pick several options, so each option is counted against the same number of people.

**Q15. Why are questions locked after the first response?**
- Old answers point to question and option ids. Changing or deleting them would break the old data.

**Q16. How does delete work?**
- Delete answers, then responses, then the survey (its questions and options go by cascade).
- Children first, because of the foreign keys.

**Q17. What is `@Transactional`?**
- The whole method runs as one database transaction. If anything fails, nothing is saved halfway.

**Q18. What is Swagger / OpenAPI used for?**
- Live docs of every endpoint at `/swagger-ui.html`. You can try requests from the browser.

**Q19. What is CORS and why did you need it?**
- The browser blocks calls from localhost:5174 to localhost:8081 unless the server allows it.
- `WebConfig` allows the React app's URL.

**Q20. Which status codes do you return?**
- 200 OK, 201 Created, 204 No Content, 400 bad input, 404 not found, 500 server error.

**Q21. How did you test it?**
- Integration tests with MockMvc and an H2 in-memory database that call the real endpoints.
- Unit tests for small rules like percentage rounding.
- Postman collection, and Vitest tests on the React side.

**Q22. Why H2 in tests but PostgreSQL in the real app?**
- H2 runs in memory, so tests are fast and need no setup. Each test starts clean.

**Q23. How does the React app show the right input for each question?**
- `QuestionField` checks `question.type`: text box for SHORT_TEXT, radio buttons for SINGLE_CHOICE,
  checkboxes for MULTIPLE_CHOICE.

**Q24. How are server errors shown under the right question?**
- The server returns `fieldErrors` like `"answers.12": "This question is required"`.
- The page reads the id after `answers.` and shows the message under that question.

**Q25. Where is the database password stored?**
- In an environment variable (`DB_PASSWORD`), not in code or Git.

**Q26. What would you add next?**
- Login for survey owners, one response per person, export results to CSV, charts.
- Flyway migrations instead of `ddl-auto=update`, and pagination for big response lists.

**Q27. What was a challenge?**
- Example answer: storing multiple-choice answers. I saved one row per selected option, which made counting
  each option a simple count instead of parsing text.

---

## 8. Files to open first when explaining

1. `backend/.../entity/Survey.java`, `Question.java`, `Answer.java` — data model.
2. `backend/.../controller/SurveyController.java`, `ResponseController.java` — endpoints.
3. `backend/.../service/SurveyService.java` — create/update rules and question locking.
4. `backend/.../service/ResponseService.java` — answer validation and summary.
5. `frontend/src/components/QuestionField.jsx` — renders each question type.
6. `frontend/src/utils/validation.js` — client-side rules.
7. `frontend/src/pages/ResponseSummaryPage.jsx` — results page.
