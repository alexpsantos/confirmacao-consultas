package br.com.confirmacao.audit.infrastructure;

import br.com.confirmacao.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByResourceTenantId(UUID tenantId, Pageable pageable);
}
