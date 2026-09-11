package br.com.confirmacao.patient.api;

import br.com.confirmacao.patient.domain.ConsentStatus;
import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.patient.domain.PreferredContactChannel;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientResponse(UUID id, UUID tenantId, String fullName, LocalDate birthDate,
                              String phone, String email, PreferredContactChannel preferredChannel,
                              ConsentStatus consentStatus, Instant consentedAt, boolean active,
                              Instant createdAt, Instant updatedAt) {
    public static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getTenant().getId(), patient.getFullName(),
                patient.getBirthDate(), patient.getPhone(), patient.getEmail(), patient.getPreferredChannel(),
                patient.getConsentStatus(), patient.getConsentedAt(), patient.isActive(),
                patient.getCreatedAt(), patient.getUpdatedAt());
    }
}
