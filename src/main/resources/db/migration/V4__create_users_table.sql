CREATE TABLE users (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    professional_id UUID,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_users_professional
        FOREIGN KEY (professional_id)
        REFERENCES professionals(id),

    CONSTRAINT uk_users_tenant_email
        UNIQUE (tenant_id, email),

    CONSTRAINT uk_users_professional
        UNIQUE (professional_id),

    CONSTRAINT ck_users_role
        CHECK (role IN ('OWNER', 'ADMIN', 'PROFESSIONAL'))
);

CREATE INDEX idx_users_tenant_id
    ON users(tenant_id);

CREATE INDEX idx_users_email
    ON users(email);