package br.com.confirmacao.tenant.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.shared.api.PageResponse;
import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;
    private final AuditService auditService;

    public TenantController(TenantService tenantService, AuditService auditService) {
        this.tenantService = tenantService;
        this.auditService = auditService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse create(
            @Valid @RequestBody CreateTenantRequest request,
            HttpServletRequest httpRequest
    ) {
        Tenant tenant = tenantService.create(
                request.displayName(),
                request.timezone()
        );

        auditService.recordAuthenticated(AuditAction.TENANT_CREATED,
                tenant.getId(), "TENANT", tenant.getId(), httpRequest);

        return TenantResponse.from(tenant);
    }

    @GetMapping
    public PageResponse<TenantResponse> findAll(
            @RequestParam(required = false) Boolean active,
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "displayName"
            ) Pageable pageable
    ) {
        Page<TenantResponse> result =
                tenantService.findAll(active, pageable)
                        .map(TenantResponse::from);

        return PageResponse.from(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@tenantAuthorization.canAccess(authentication, #id)")
    public ResponseEntity<TenantResponse> findById(
            @PathVariable UUID id
    ) {
        Tenant tenant = tenantService.findById(id);

        TenantResponse response = TenantResponse.from(tenant);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@tenantAuthorization.canAccess(authentication, #id)")
    public ResponseEntity<TenantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request,
            HttpServletRequest httpRequest
    ) {
        Tenant tenant = tenantService.update(
                id,
                request.displayName(),
                request.timezone()
        );

        auditService.recordAuthenticated(AuditAction.TENANT_UPDATED,
                id, "TENANT", id, httpRequest);

        TenantResponse response = TenantResponse.from(tenant);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id,
            HttpServletRequest httpRequest
    ) {
        tenantService.deactivate(id);
        auditService.recordAuthenticated(AuditAction.TENANT_DEACTIVATED,
                id, "TENANT", id, httpRequest);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable UUID id,
            HttpServletRequest httpRequest
    ) {
        tenantService.activate(id);
        auditService.recordAuthenticated(AuditAction.TENANT_ACTIVATED,
                id, "TENANT", id, httpRequest);

        return ResponseEntity.noContent().build();
    }
}
