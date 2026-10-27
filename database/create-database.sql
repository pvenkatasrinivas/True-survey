-- ============================================================
-- Step 1 (one time): create the database.
-- Run as the postgres superuser, for example:
--   psql -U postgres -f database/create-database.sql
--
-- Optional: create a separate login for the app instead of using "postgres".
-- Replace CHANGE_ME with your own password, then set DB_USERNAME=truesurvey_app
-- and DB_PASSWORD=<that password> before starting the backend.
-- ============================================================

CREATE DATABASE truesurvey;

-- CREATE USER truesurvey_app WITH PASSWORD 'CHANGE_ME';
-- GRANT ALL PRIVILEGES ON DATABASE truesurvey TO truesurvey_app;
-- (PostgreSQL 15+) also run inside the truesurvey database:
--   \c truesurvey
--   GRANT ALL ON SCHEMA public TO truesurvey_app;
