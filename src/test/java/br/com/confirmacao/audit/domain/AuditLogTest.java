package br.com.confirmacao.audit.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    @Test
    void shouldCreateAuditLog() {
        UUID tenantId = UUID.randomUUID();
        AuditLog log = new AuditLog(UUID.randomUUID(), tenantId, tenantId,
                AuditAction.TENANT_UPDATED, "TENANT", tenantId, "127.0.0.1");

        assertNotNull(log.getId());
        assertNotNull(log.getCreatedAt());
        assertEquals(tenantId, log.getResourceTenantId());
        assertEquals(AuditAction.TENANT_UPDATED, log.getAction());
    }

    @Test
    void shouldRejectMissingRequiredData() {
        assertThrows(IllegalArgumentException.class, () ->
                new AuditLog(null, null, null, null, " ", null, null));
    }
}
