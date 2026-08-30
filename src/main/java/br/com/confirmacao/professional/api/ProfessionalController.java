package br.com.confirmacao.professional.api;


import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.domain.Professional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @PostMapping
    public ResponseEntity<ProfessionalResponse> create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateProfessionalRequest request
    ) {
        Optional<Professional> professionalOptional = professionalService.create(
                        tenantId,
                        request.fullName(),
                        request.email(),
                        request.phone(),
                        request.registrationNumber()
                );

        if (professionalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Professional professional = professionalOptional.get();
        ProfessionalResponse response = ProfessionalResponse.from(professional);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProfessionalResponse>> findAll(@PathVariable UUID tenantId) {
        Optional<List<Professional>> professionalsOptional = professionalService.findAll(tenantId);

        if (professionalsOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProfessionalResponse> responses = professionalsOptional.get()
                .stream()
                .map(ProfessionalResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{professionalId}")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable UUID tenantId,@PathVariable UUID professionalId) {
        Optional<Professional> professionalOptional = professionalService.findById(tenantId, professionalId);

        if (professionalOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ProfessionalResponse response = ProfessionalResponse.from(professionalOptional.get());

        return ResponseEntity.ok(response);
    }

}