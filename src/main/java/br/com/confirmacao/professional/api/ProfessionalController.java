package br.com.confirmacao.professional.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.access.ProfessionalAccessService;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.shared.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/professionals")
@PreAuthorize("@tenantAuthorization.canAccess(authentication, #tenantId)")
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final ProfessionalAccessService professionalAccessService;
    private final AuditService auditService;

    public ProfessionalController(
            ProfessionalService professionalService,
            ProfessionalAccessService professionalAccessService,
            AuditService auditService
    ) {
        this.professionalService = professionalService;
        this.professionalAccessService = professionalAccessService;
        this.auditService = auditService;
    }

    @PostMapping("/with-access")
    public ResponseEntity<ProfessionalResponse> createWithAccess(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateProfessionalAccessRequest request,
            HttpServletRequest httpRequest) {
        var result = professionalAccessService.create(tenantId,
                request.fullName(), request.email(), request.phone(),
                request.registrationNumber(), request.password());
        auditService.recordAuthenticated(AuditAction.PROFESSIONAL_CREATED,
                tenantId, "PROFESSIONAL", result.professional().getId(), httpRequest);
        auditService.recordAuthenticated(AuditAction.USER_CREATED,
                tenantId, "USER", result.user().getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfessionalResponse.from(result.professional()));
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateProfessionalRequest request,
            HttpServletRequest httpRequest
    ) {
        Professional professional = professionalService.create(
                tenantId,
                request.fullName(),
                request.email(),
                request.phone(),
                request.registrationNumber()
        );
        auditService.recordAuthenticated(AuditAction.PROFESSIONAL_CREATED,
                tenantId, "PROFESSIONAL", professional.getId(), httpRequest);

        ProfessionalResponse response =
                ProfessionalResponse.from(professional);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<PageResponse<ProfessionalResponse>> findAll(
            @PathVariable UUID tenantId,
            @ParameterObject
            @PageableDefault(
                    size = 20,
                    sort = "fullName"
            ) Pageable pageable
    ) {
        Page<ProfessionalResponse> responses =
                professionalService
                        .findAll(tenantId, pageable)
                        .map(ProfessionalResponse::from);

        PageResponse<ProfessionalResponse> response =
                PageResponse.from(responses);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{professionalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') or @tenantAuthorization.isProfessional(authentication, #professionalId)")
    public ResponseEntity<ProfessionalResponse> findById(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId
    ) {
        Professional professional =
                professionalService.findById(
                        tenantId,
                        professionalId
                );

        ProfessionalResponse response =
                ProfessionalResponse.from(professional);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{professionalId}")
    public ResponseEntity<ProfessionalResponse> update(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId,
            @Valid @RequestBody UpdateProfessionalRequest request,
            HttpServletRequest httpRequest
    ) {
        Professional professional = professionalService.update(
                tenantId,
                professionalId,
                request.fullName(),
                request.email(),
                request.phone(),
                request.registrationNumber()
        );
        auditService.recordAuthenticated(AuditAction.PROFESSIONAL_UPDATED,
                tenantId, "PROFESSIONAL", professionalId, httpRequest);

        ProfessionalResponse response =
                ProfessionalResponse.from(professional);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{professionalId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId,
            HttpServletRequest httpRequest
    ) {
        professionalService.deactivate(
                tenantId,
                professionalId
        );
        auditService.recordAuthenticated(AuditAction.PROFESSIONAL_DEACTIVATED,
                tenantId, "PROFESSIONAL", professionalId, httpRequest);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{professionalId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId,
            HttpServletRequest httpRequest
    ) {
        professionalService.activate(
                tenantId,
                professionalId
        );
        auditService.recordAuthenticated(AuditAction.PROFESSIONAL_ACTIVATED,
                tenantId, "PROFESSIONAL", professionalId, httpRequest);

        return ResponseEntity.noContent().build();
    }
}
