CREATE TABLE histories (
    id SERIAL NOT NULL PRIMARY KEY,
    sub BIGINT NOT NULL,
    session_time TIMESTAMP NOT NULL,
    plan_study_time INTEGER NOT NULL,
    plan_break_time INTEGER NOT NULL,
    study_time INTEGER NOT NULL,
    break_time INTEGER NOT NULL
)