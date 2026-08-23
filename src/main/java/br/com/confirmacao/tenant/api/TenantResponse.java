package br.com.confirmacao.tenant.api;

import br.com.confirmacao.tenant.domain.Tenant;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String displayName,
        String timezone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getDisplayName(),
                tenant.getTimezone(),
                tenant.isActive(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}