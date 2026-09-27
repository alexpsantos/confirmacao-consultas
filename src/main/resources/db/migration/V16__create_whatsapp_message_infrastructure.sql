ALTER TABLE patients
    ADD COLUMN whatsapp_consent_source VARCHAR(30);

UPDATE patients
SET consent_status = 'GRANTED',
    consented_at = COALESCE(consented_at, created_at),
    whatsapp_consent_source = 'PROFESSIONAL_DECLARATION'
WHERE consent_status = 'PENDING';

CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES sessions(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    professional_id UUID NOT NULL REFERENCES professionals(id),
    meta_message_id VARCHAR(120),
    idempotency_key VARCHAR(160) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    delivery_status VARCHAR(20) NOT NULL,
    confirmation_status VARCHAR(20) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_whatsapp_messages_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uk_whatsapp_messages_meta_message UNIQUE (meta_message_id),
    CONSTRAINT ck_whatsapp_delivery_status CHECK (delivery_status IN ('QUEUED', 'SENT', 'DELIVERED', 'READ', 'FAILED')),
    CONSTRAINT ck_whatsapp_confirmation_status CHECK (confirmation_status IN ('PENDING', 'CONFIRMED', 'CANCELED'))
);

CREATE INDEX idx_whatsapp_messages_session ON whatsapp_messages(session_id);
CREATE INDEX idx_whatsapp_messages_professional_created ON whatsapp_messages(professional_id, created_at DESC);
