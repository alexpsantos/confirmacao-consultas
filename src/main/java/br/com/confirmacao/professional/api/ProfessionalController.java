package br.com.confirmacao.professional.api;

import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.domain.Professional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(
            ProfessionalService professionalService
    ) {
        this.professionalService = professionalService;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateProfessionalRequest request
    ) {
        Professional professional = professionalService.create(
                tenantId,
                request.fullName(),
                request.email(),
                request.phone(),
                request.registrationNumber()
        );

        ProfessionalResponse response =
                ProfessionalResponse.from(professional);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll(
            @PathVariable UUID tenantId
    ) {
        List<ProfessionalResponse> responses =
                professionalService.findAll(tenantId)
                        .stream()
                        .map(ProfessionalResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{professionalId}")
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
            @Valid @RequestBody UpdateProfessionalRequest request
    ) {
        Professional professional = professionalService.update(
                tenantId,
                professionalId,
                request.fullName(),
                request.email(),
                request.phone(),
                request.registrationNumber()
        );

        ProfessionalResponse response =
                ProfessionalResponse.from(professional);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{professionalId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId
    ) {
        professionalService.deactivate(
                tenantId,
                professionalId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{professionalId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable UUID tenantId,
            @PathVariable UUID professionalId
    ) {
        professionalService.activate(
                tenantId,
                professionalId
        );

        return ResponseEntity.noContent().build();
    }
}