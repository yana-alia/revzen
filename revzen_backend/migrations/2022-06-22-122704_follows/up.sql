CREATE TABLE follows (
    followee BIGINT NOT NULL,
    follower BIGINT NOT NULL,
    accepted BOOL NOT NULL,
    PRIMARY KEY(followee, follower)
)