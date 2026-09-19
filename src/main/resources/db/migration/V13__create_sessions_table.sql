CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    professional_id UUID NOT NULL REFERENCES professionals(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    modality VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    meeting_link VARCHAR(500),
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_sessions_period CHECK (ends_at > starts_at),
    CONSTRAINT ck_sessions_modality CHECK (modality IN ('PRESENTIAL', 'ONLINE')),
    CONSTRAINT ck_sessions_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELED', 'NO_SHOW'))
);

CREATE INDEX idx_sessions_professional_period ON sessions(professional_id, starts_at, ends_at);
CREATE INDEX idx_sessions_patient ON sessions(patient_id);
