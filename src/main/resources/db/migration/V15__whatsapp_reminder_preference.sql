ALTER TABLE patients
    ADD COLUMN whatsapp_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN whatsapp_reminders_updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN whatsapp_reminders_source VARCHAR(30) NOT NULL DEFAULT 'DEFAULT';

UPDATE patients
SET whatsapp_reminders_enabled = FALSE,
    whatsapp_reminders_updated_at = updated_at,
    whatsapp_reminders_source = 'LEGACY_REVOKED'
WHERE consent_status = 'REVOKED';
