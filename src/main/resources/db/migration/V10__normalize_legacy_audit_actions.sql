-- Preserva eventos históricos criados antes do modelo profissional-individual,
-- usando nomes neutros que não reintroduzem regras ou entidades removidas.
UPDATE audit_logs SET action = 'LEGACY_ORGANIZATION_CREATED'
WHERE action = 'TENANT_CREATED';

UPDATE audit_logs SET action = 'LEGACY_ORGANIZATION_UPDATED'
WHERE action = 'TENANT_UPDATED';

UPDATE audit_logs SET action = 'LEGACY_ORGANIZATION_ACTIVATED'
WHERE action = 'TENANT_ACTIVATED';

UPDATE audit_logs SET action = 'LEGACY_ORGANIZATION_DEACTIVATED'
WHERE action = 'TENANT_DEACTIVATED';
