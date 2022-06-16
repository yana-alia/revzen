-- friends table's friend_status is:
-- 0 => Currently Friends
-- 1 => User_a requested
-- 2 => User_b requested
CREATE TABLE friends (
    user_a BIGINT NOT NULL,
    user_b BIGINT NOT NULL,
    friend_status INTEGER NOT NULL,
    PRIMARY KEY(user_a, user_b)
)