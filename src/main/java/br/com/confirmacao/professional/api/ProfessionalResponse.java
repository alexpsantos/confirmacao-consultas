package br.com.confirmacao.professional.api;

import br.com.confirmacao.professional.domain.Professional;

import java.time.Instant;
import java.util.UUID;

public record ProfessionalResponse(UUID id,
                                   UUID tenantId,
                                   String fullName,
                                   String email,
                                   String phone,
                                   String registrationNumber,
                                   boolean active,
                                   Instant createdAt,
                                   Instant updatedAt) {




    public static ProfessionalResponse from(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getTenant().getId(),
                professional.getFullName(),
                professional.getEmail(),
                professional.getPhone(),
                professional.getRegistrationNumber(),
                professional.isActive(),
                professional.getCreatedAt(),
                professional.getUpdatedAt()
        );
    }

}
