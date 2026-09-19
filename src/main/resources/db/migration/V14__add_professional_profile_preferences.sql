ALTER TABLE professionals
    ADD COLUMN specialty VARCHAR(100),
    ADD COLUMN display_name VARCHAR(150),
    ADD COLUMN timezone VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo',
    ADD COLUMN default_session_minutes INTEGER NOT NULL DEFAULT 60,
    ADD COLUMN default_modality VARCHAR(20) NOT NULL DEFAULT 'PRESENTIAL',
    ADD COLUMN reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE;
