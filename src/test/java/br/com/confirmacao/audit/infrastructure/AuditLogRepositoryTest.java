package br.com.confirmacao.audit.infrastructure;

import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.audit.domain.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AuditLogRepositoryTest {
    @Autowired private AuditLogRepository repository;

    @Test
    void shouldListOnlyLogsFromRequestedTenant() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        repository.save(new AuditLog(null, null, tenantA,
                AuditAction.LOGIN_FAILURE, "AUTH", null, "127.0.0.1"));
        repository.save(new AuditLog(null, null, tenantB,
                AuditAction.LOGIN_FAILURE, "AUTH", null, "127.0.0.1"));

        var result = repository.findAllByResourceTenantId(
                tenantA, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(tenantA, result.getContent().getFirst().getResourceTenantId());
    }
}
