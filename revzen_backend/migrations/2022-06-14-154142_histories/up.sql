CREATE TABLE histories (
    id SERIAL NOT NULL PRIMARY KEY,
    sub BIGINT NOT NULL,
    session_time TIMESTAMP,
    plan_study_time INTEGER,
    plan_break_time INTEGER,
    study_time INTEGER,
    break_time INTEGER
)