package br.com.confirmacao.patient.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.patient.application.PatientService;
import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.shared.api.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/patients")
@PreAuthorize("@tenantAuthorization.canAccess(authentication, #tenantId)")
public class PatientController {
    private final PatientService patientService;
    private final AuditService auditService;

    public PatientController(PatientService patientService, AuditService auditService) {
        this.patientService = patientService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@PathVariable UUID tenantId,
            @Valid @RequestBody CreatePatientRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientService.create(tenantId, request.fullName(), request.birthDate(),
                request.phone(), request.email(), request.preferredChannel());
        auditService.recordAuthenticated(AuditAction.PATIENT_CREATED, tenantId, "PATIENT", patient.getId(), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientResponse.from(patient));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PatientResponse>> findAll(@PathVariable UUID tenantId,
            @ParameterObject @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(patientService.findAll(tenantId, pageable).map(PatientResponse::from)));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> findById(@PathVariable UUID tenantId, @PathVariable UUID patientId) {
        return ResponseEntity.ok(PatientResponse.from(patientService.findById(tenantId, patientId)));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> update(@PathVariable UUID tenantId, @PathVariable UUID patientId,
            @Valid @RequestBody UpdatePatientRequest request, HttpServletRequest httpRequest) {
        Patient patient = patientService.update(tenantId, patientId, request.fullName(), request.birthDate(),
                request.phone(), request.email(), request.preferredChannel());
        auditService.recordAuthenticated(AuditAction.PATIENT_UPDATED, tenantId, "PATIENT", patientId, httpRequest);
        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID tenantId, @PathVariable UUID patientId,
            HttpServletRequest httpRequest) {
        patientService.deactivate(tenantId, patientId);
        auditService.recordAuthenticated(AuditAction.PATIENT_DEACTIVATED, tenantId, "PATIENT", patientId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{patientId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID tenantId, @PathVariable UUID patientId,
            HttpServletRequest httpRequest) {
        patientService.activate(tenantId, patientId);
        auditService.recordAuthenticated(AuditAction.PATIENT_ACTIVATED, tenantId, "PATIENT", patientId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{patientId}/consent/grant")
    public ResponseEntity<PatientResponse> grantConsent(@PathVariable UUID tenantId, @PathVariable UUID patientId,
            HttpServletRequest httpRequest) {
        Patient patient = patientService.grantConsent(tenantId, patientId);
        auditService.recordAuthenticated(AuditAction.PATIENT_CONSENT_GRANTED, tenantId, "PATIENT", patientId, httpRequest);
        return ResponseEntity.ok(PatientResponse.from(patient));
    }

    @PatchMapping("/{patientId}/consent/revoke")
    public ResponseEntity<PatientResponse> revokeConsent(@PathVariable UUID tenantId, @PathVariable UUID patientId,
            HttpServletRequest httpRequest) {
        Patient patient = patientService.revokeConsent(tenantId, patientId);
        auditService.recordAuthenticated(AuditAction.PATIENT_CONSENT_REVOKED, tenantId, "PATIENT", patientId, httpRequest);
        return ResponseEntity.ok(PatientResponse.from(patient));
    }
}
