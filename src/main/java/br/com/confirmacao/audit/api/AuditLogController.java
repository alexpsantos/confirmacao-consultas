package br.com.confirmacao.audit.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.shared.api.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/audit-logs")
public class AuditLogController {
    private final AuditService auditService;

    public AuditLogController(AuditService auditService) { this.auditService = auditService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') and "
            + "@tenantAuthorization.canAccess(authentication, #tenantId)")
    public PageResponse<AuditLogResponse> findAll(
            @PathVariable UUID tenantId,
            @ParameterObject @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {
        return PageResponse.from(auditService.findAll(tenantId, pageable)
                .map(AuditLogResponse::from));
    }
}
