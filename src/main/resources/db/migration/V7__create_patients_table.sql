CREATE TABLE patients (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    birth_date DATE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(254),
    preferred_channel VARCHAR(20) NOT NULL DEFAULT 'WHATSAPP',
    consent_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    consented_at TIMESTAMP WITH TIME ZONE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_patients_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id),

    CONSTRAINT uk_patients_tenant_phone
        UNIQUE (tenant_id, phone),

    CONSTRAINT ck_patients_birth_date
        CHECK (birth_date IS NULL OR birth_date <= CURRENT_DATE),

    CONSTRAINT ck_patients_preferred_channel
        CHECK (preferred_channel IN ('WHATSAPP', 'EMAIL')),

    CONSTRAINT ck_patients_consent_status
        CHECK (consent_status IN ('PENDING', 'GRANTED', 'REVOKED')),

    CONSTRAINT ck_patients_consent_date
        CHECK (consent_status <> 'GRANTED' OR consented_at IS NOT NULL)
);

CREATE INDEX idx_patients_tenant_id
    ON patients(tenant_id);

CREATE INDEX idx_patients_tenant_active
    ON patients(tenant_id, active);

CREATE INDEX idx_patients_tenant_full_name
    ON patients(tenant_id, full_name);
