package br.com.confirmacao.audit.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id private UUID id;
    @Column(name = "actor_user_id") private UUID actorUserId;
    @Column(name = "actor_tenant_id") private UUID actorTenantId;
    @Column(name = "resource_tenant_id") private UUID resourceTenantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 60) private AuditAction action;
    @Column(name = "resource_type", nullable = false, length = 60) private String resourceType;
    @Column(name = "resource_id") private UUID resourceId;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AuditLog() {}

    public AuditLog(UUID actorUserId, UUID actorTenantId, UUID resourceTenantId,
                    AuditAction action, String resourceType, UUID resourceId,
                    String ipAddress) {
        if (action == null || resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("Dados obrigatórios da auditoria não informados");
        }
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.actorTenantId = actorTenantId;
        this.resourceTenantId = resourceTenantId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getActorUserId() { return actorUserId; }
    public UUID getActorTenantId() { return actorTenantId; }
    public UUID getResourceTenantId() { return resourceTenantId; }
    public AuditAction getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public String getIpAddress() { return ipAddress; }
    public Instant getCreatedAt() { return createdAt; }
}
