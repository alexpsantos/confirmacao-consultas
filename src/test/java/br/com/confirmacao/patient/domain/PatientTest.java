package br.com.confirmacao.patient.domain;

import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void shouldCreatePatientWithNormalizedDataAndPendingConsent() {
        Patient patient = new Patient(
                tenant(), "  Ana Souza  ", LocalDate.of(1990, 5, 10),
                "(11) 99999-8888", " ANA@EXEMPLO.COM ",
                PreferredContactChannel.WHATSAPP
        );

        assertEquals("Ana Souza", patient.getFullName());
        assertEquals("11999998888", patient.getPhone());
        assertEquals("ana@exemplo.com", patient.getEmail());
        assertEquals(ConsentStatus.PENDING, patient.getConsentStatus());
        assertNull(patient.getConsentedAt());
        assertTrue(patient.isActive());
    }

    @Test
    void shouldRejectFutureBirthDate() {
        assertThrows(IllegalArgumentException.class, () -> new Patient(
                tenant(), "Ana Souza", LocalDate.now().plusDays(1),
                "11999998888", null, PreferredContactChannel.WHATSAPP
        ));
    }

    @Test
    void shouldRejectInvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> new Patient(
                tenant(), "Ana Souza", null,
                "123", null, PreferredContactChannel.WHATSAPP
        ));
    }

    @Test
    void shouldGrantAndRevokeConsent() {
        Patient patient = patient();

        patient.grantConsent();
        assertEquals(ConsentStatus.GRANTED, patient.getConsentStatus());
        assertNotNull(patient.getConsentedAt());

        patient.revokeConsent();
        assertEquals(ConsentStatus.REVOKED, patient.getConsentStatus());
        assertNull(patient.getConsentedAt());
    }

    @Test
    void shouldDeactivateAndReactivatePatient() {
        Patient patient = patient();
        patient.deactivate();
        assertFalse(patient.isActive());
        patient.activate();
        assertTrue(patient.isActive());
    }

    private Patient patient() {
        return new Patient(
                tenant(), "Ana Souza", null,
                "11999998888", null, PreferredContactChannel.WHATSAPP
        );
    }

    private Tenant tenant() {
        return new Tenant("Clínica Horizonte", "America/Sao_Paulo");
    }
}
