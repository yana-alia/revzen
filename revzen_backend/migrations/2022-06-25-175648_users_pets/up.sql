ALTER TABLE users ADD COLUMN main_pet INT;
UPDATE users SET main_pet = 0;
ALTER TABLE users ALTER COLUMN main_pet SET NOT NULL;