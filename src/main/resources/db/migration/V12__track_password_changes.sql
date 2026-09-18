ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP WITH TIME ZONE;
UPDATE users SET password_changed_at = created_at;
ALTER TABLE users ALTER COLUMN password_changed_at SET NOT NULL;
