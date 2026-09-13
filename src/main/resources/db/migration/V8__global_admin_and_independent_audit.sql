-- Administradores da plataforma são identidades globais e não pertencem a clínicas.
ALTER TABLE users ALTER COLUMN tenant_id DROP NOT NULL;

UPDATE users
SET tenant_id = NULL,
    professional_id = NULL,
    updated_at = CURRENT_TIMESTAMP
WHERE role = 'ADMIN';

ALTER TABLE users ADD CONSTRAINT ck_users_tenant_scope
    CHECK (
        (role = 'ADMIN' AND tenant_id IS NULL AND professional_id IS NULL)
        OR
        (role IN ('OWNER', 'PROFESSIONAL') AND tenant_id IS NOT NULL)
    );

-- Audit logs são snapshots históricos. Estes campos são UUIDs deliberadamente
-- sem foreign keys, para sobreviver à remoção futura das entidades de origem.
ALTER TABLE audit_logs ALTER COLUMN resource_tenant_id DROP NOT NULL;
