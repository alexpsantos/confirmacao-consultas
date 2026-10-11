package br.com.confirmacao;

import br.com.confirmacao.admin.api.AdminOperationalSummaryController;
import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminOperationalSummaryControllerTest {
    @Test
    void returnsOnlyOperationalCountsForTheSelectedPeriod() {
        var professionals = mock(ProfessionalRepository.class);
        var patients = mock(PatientRepository.class);
        var sessions = mock(SessionAppointmentRepository.class);
        var start = Instant.parse("2026-10-01T00:00:00Z");
        var end = Instant.parse("2026-11-01T00:00:00Z");
        when(professionals.countByActiveTrue()).thenReturn(4L);
        when(patients.countByActiveTrue()).thenReturn(18L);
        when(sessions.countByStartsAtGreaterThanEqualAndStartsAtLessThan(start, end)).thenReturn(32L);
        when(sessions.countByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatus(start, end, SessionStatus.CANCELED)).thenReturn(3L);

        var result = new AdminOperationalSummaryController(professionals, patients, sessions).summary(start, end);

        assertEquals(4L, result.activeProfessionals());
        assertEquals(18L, result.activePatients());
        assertEquals(32L, result.sessions());
        assertEquals(3L, result.canceledSessions());
    }

    @Test
    void rejectsInvalidPeriod() {
        var controller = new AdminOperationalSummaryController(mock(ProfessionalRepository.class), mock(PatientRepository.class), mock(SessionAppointmentRepository.class));
        var instant = Instant.parse("2026-10-01T00:00:00Z");
        assertThrows(IllegalArgumentException.class, () -> controller.summary(instant, instant));
    }
}
