-- Migra o modelo legado para profissionais independentes, preservando V1-V8.
UPDATE users u SET professional_id = (
  SELECT MIN(p.id::text)::uuid FROM professionals p
  WHERE LOWER(p.email) = LOWER(u.email) AND p.tenant_id = u.tenant_id
    AND NOT EXISTS (SELECT 1 FROM users linked WHERE linked.professional_id = p.id)
)
WHERE u.role = 'OWNER' AND u.professional_id IS NULL
  AND EXISTS (SELECT 1 FROM professionals p WHERE LOWER(p.email) = LOWER(u.email)
    AND p.tenant_id = u.tenant_id AND NOT EXISTS (SELECT 1 FROM users linked WHERE linked.professional_id = p.id));

INSERT INTO professionals (id, tenant_id, full_name, email, phone, registration_number, active, created_at, updated_at)
SELECT u.id, u.tenant_id, u.name, u.email, NULL, NULL, u.active, u.created_at, u.updated_at
FROM users u
WHERE u.role = 'OWNER' AND u.professional_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM professionals p WHERE p.id = u.id);

UPDATE users SET professional_id = id WHERE role = 'OWNER' AND professional_id IS NULL;
UPDATE users SET role = 'PROFESSIONAL' WHERE role = 'OWNER';

-- O modelo anterior aceitava o mesmo e-mail/registro em organizações diferentes.
-- Mantém o registro mais antigo intacto e torna os demais identificadores únicos.
UPDATE users u SET email = CONCAT(SUBSTRING(u.email, 1, POSITION('@' IN u.email) - 1),
  '+', CAST(u.id AS VARCHAR), SUBSTRING(u.email, POSITION('@' IN u.email)))
WHERE EXISTS (SELECT 1 FROM users older WHERE LOWER(older.email) = LOWER(u.email) AND older.id < u.id);
UPDATE professionals p SET email = CONCAT(SUBSTRING(p.email, 1, POSITION('@' IN p.email) - 1),
  '+', CAST(p.id AS VARCHAR), SUBSTRING(p.email, POSITION('@' IN p.email)))
WHERE EXISTS (SELECT 1 FROM professionals older WHERE LOWER(older.email) = LOWER(p.email) AND older.id < p.id);
UPDATE professionals p SET registration_number = NULL
WHERE p.registration_number IS NOT NULL AND EXISTS (
  SELECT 1 FROM professionals older WHERE older.registration_number = p.registration_number AND older.id < p.id
);

-- Uma organização antiga sem profissional pode ainda possuir pacientes. Seu UUID
-- vira o UUID de um perfil técnico, evitando perda de prontuários.
INSERT INTO professionals (id, tenant_id, full_name, email, phone, registration_number, active, created_at, updated_at)
SELECT t.id, t.id, 'Profissional migrado', CONCAT('migrado+', t.id, '@invalid.local'), NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tenants t
WHERE EXISTS (SELECT 1 FROM patients p WHERE p.tenant_id = t.id)
  AND NOT EXISTS (SELECT 1 FROM professionals p WHERE p.tenant_id = t.id);

ALTER TABLE patients ADD COLUMN professional_id UUID;
UPDATE patients p SET professional_id = (
  SELECT MIN(pr.id::text)::uuid FROM professionals pr WHERE pr.tenant_id = p.tenant_id
);
ALTER TABLE patients ALTER COLUMN professional_id SET NOT NULL;

ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_tenant_scope;
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_tenant;
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_tenant_email;
ALTER TABLE professionals DROP CONSTRAINT IF EXISTS fk_professionals_tenant;
ALTER TABLE professionals DROP CONSTRAINT IF EXISTS uk_professionals_tenant_email;
ALTER TABLE professionals DROP CONSTRAINT IF EXISTS uk_professionals_tenant_registration;
ALTER TABLE patients DROP CONSTRAINT IF EXISTS fk_patients_tenant;
ALTER TABLE patients DROP CONSTRAINT IF EXISTS uk_patients_tenant_phone;

DROP INDEX IF EXISTS idx_users_tenant_id;
DROP INDEX IF EXISTS idx_professionals_tenant_id;
DROP INDEX IF EXISTS idx_patients_tenant_id;
DROP INDEX IF EXISTS idx_patients_tenant_active;
DROP INDEX IF EXISTS idx_patients_tenant_full_name;
DROP INDEX IF EXISTS idx_audit_logs_resource_tenant_created_at;

ALTER TABLE users DROP COLUMN tenant_id;
ALTER TABLE professionals DROP COLUMN tenant_id;
ALTER TABLE patients DROP COLUMN tenant_id;
ALTER TABLE audit_logs DROP COLUMN actor_tenant_id;
ALTER TABLE audit_logs DROP COLUMN resource_tenant_id;
DROP TABLE tenants;

ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_role;
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'PROFESSIONAL'));
ALTER TABLE users ADD CONSTRAINT ck_users_professional_scope CHECK (
  (role = 'ADMIN' AND professional_id IS NULL) OR
  (role = 'PROFESSIONAL' AND professional_id IS NOT NULL)
);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE professionals ADD CONSTRAINT uk_professionals_email UNIQUE (email);
ALTER TABLE professionals ADD CONSTRAINT uk_professionals_registration UNIQUE (registration_number);
ALTER TABLE patients ADD CONSTRAINT fk_patients_professional FOREIGN KEY (professional_id) REFERENCES professionals(id);
ALTER TABLE patients ADD CONSTRAINT uk_patients_professional_phone UNIQUE (professional_id, phone);
CREATE INDEX idx_patients_professional_id ON patients(professional_id);
CREATE INDEX idx_patients_professional_active ON patients(professional_id, active);
