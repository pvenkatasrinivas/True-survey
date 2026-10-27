-- ============================================================
-- Optional sample survey with two responses, so the app is not empty on first run.
-- Run AFTER the backend has started once (so the tables exist):
--   psql -U postgres -d truesurvey -f database/sample-data.sql
-- (Easier alternative: create a survey from the UI or with Postman.)
-- ============================================================

DO $$
DECLARE
    s_id BIGINT; q_name BIGINT; q_food BIGINT; q_meals BIGINT;
    o_good BIGINT; o_avg BIGINT; o_poor BIGINT; o_bf BIGINT; o_lunch BIGINT; o_dinner BIGINT;
    r1 BIGINT; r2 BIGINT;
BEGIN
    INSERT INTO surveys (title, description, created_at, updated_at)
    VALUES ('Canteen Feedback', 'Tell us what you think about the office canteen', NOW(), NOW())
    RETURNING id INTO s_id;

    INSERT INTO questions (survey_id, text, type, required, position)
    VALUES (s_id, 'Your name (optional)', 'SHORT_TEXT', FALSE, 1) RETURNING id INTO q_name;
    INSERT INTO questions (survey_id, text, type, required, position)
    VALUES (s_id, 'How was the food?', 'SINGLE_CHOICE', TRUE, 2) RETURNING id INTO q_food;
    INSERT INTO questions (survey_id, text, type, required, position)
    VALUES (s_id, 'Which meals do you eat here?', 'MULTIPLE_CHOICE', TRUE, 3) RETURNING id INTO q_meals;

    INSERT INTO question_options (question_id, text, position) VALUES (q_food, 'Good', 1)    RETURNING id INTO o_good;
    INSERT INTO question_options (question_id, text, position) VALUES (q_food, 'Average', 2) RETURNING id INTO o_avg;
    INSERT INTO question_options (question_id, text, position) VALUES (q_food, 'Poor', 3)    RETURNING id INTO o_poor;
    INSERT INTO question_options (question_id, text, position) VALUES (q_meals, 'Breakfast', 1) RETURNING id INTO o_bf;
    INSERT INTO question_options (question_id, text, position) VALUES (q_meals, 'Lunch', 2)     RETURNING id INTO o_lunch;
    INSERT INTO question_options (question_id, text, position) VALUES (q_meals, 'Dinner', 3)    RETURNING id INTO o_dinner;

    INSERT INTO survey_responses (survey_id, submitted_at) VALUES (s_id, NOW() - INTERVAL '1 day') RETURNING id INTO r1;
    INSERT INTO answers (response_id, question_id, option_id, text_value) VALUES
        (r1, q_name, NULL, 'Ravi'),
        (r1, q_food, o_good, NULL),
        (r1, q_meals, o_bf, NULL),
        (r1, q_meals, o_lunch, NULL);

    INSERT INTO survey_responses (survey_id, submitted_at) VALUES (s_id, NOW()) RETURNING id INTO r2;
    INSERT INTO answers (response_id, question_id, option_id, text_value) VALUES
        (r2, q_food, o_avg, NULL),
        (r2, q_meals, o_lunch, NULL);
END $$;
