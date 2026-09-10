CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    actor_tenant_id UUID,
    resource_tenant_id UUID NOT NULL,
    action VARCHAR(60) NOT NULL,
    resource_type VARCHAR(60) NOT NULL,
    resource_id UUID,
    ip_address VARCHAR(45),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_resource_tenant_created_at
    ON audit_logs(resource_tenant_id, created_at DESC);

CREATE INDEX idx_audit_logs_actor_user_id ON audit_logs(actor_user_id);
