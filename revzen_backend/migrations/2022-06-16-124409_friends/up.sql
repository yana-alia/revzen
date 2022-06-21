CREATE TABLE friends (
    user_a BIGINT NOT NULL,
    user_b BIGINT NOT NULL,
    friend_status INTEGER NOT NULL,
    PRIMARY KEY(user_a, user_b)
)