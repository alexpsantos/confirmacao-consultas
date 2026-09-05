ALTER TABLE professionals
    ADD CONSTRAINT uk_professionals_tenant_registration
    UNIQUE (tenant_id, registration_number);