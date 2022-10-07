CREATE TABLE pets (
    user_id BIGINT NOT NULL,
    pet_type INT NOT NULL,
    health INT NOT NULL,
    xp INT NOT NULL,
    PRIMARY KEY(user_id, pet_type)
)