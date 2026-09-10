package br.com.confirmacao.audit.api;

import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.audit.domain.AuditLog;
import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID actorUserId, UUID actorTenantId,
                               UUID resourceTenantId, AuditAction action,
                               String resourceType, UUID resourceId,
                               String ipAddress, Instant createdAt) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActorUserId(),
                log.getActorTenantId(), log.getResourceTenantId(), log.getAction(),
                log.getResourceType(), log.getResourceId(), log.getIpAddress(), log.getCreatedAt());
    }
}
