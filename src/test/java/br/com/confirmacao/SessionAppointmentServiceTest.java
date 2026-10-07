package br.com.confirmacao;

import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.patient.domain.PreferredContactChannel;
import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.application.SessionAppointmentService;
import br.com.confirmacao.session.domain.SessionAppointment;
import br.com.confirmacao.session.domain.SessionModality;
import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.scheduleblock.infrastructure.ScheduleBlockRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionAppointmentServiceTest {

    @Test
    void pastSessionAllowsOnlyResultUpdateEvenWhenPatientIsInactive() {
        var sessions = mock(SessionAppointmentRepository.class);
        var patients = mock(PatientRepository.class);
        var professionals = mock(ProfessionalRepository.class);
        var professional = new Professional("Ana", "ana@example.com", "11999999999", null);
        var patient = new Patient(professional, "Paciente", null, "11888888888", null, PreferredContactChannel.WHATSAPP);
        patient.deactivate();
        var appointment = new SessionAppointment(professional, patient,
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600),
                SessionModality.ONLINE, null, "Observação");
        when(sessions.findByIdAndProfessionalId(appointment.getId(), professional.getId()))
                .thenReturn(Optional.of(appointment));
        var service = new SessionAppointmentService(sessions, patients, professionals,mock(ScheduleBlockRepository.class));

        var updated = service.updateResult(professional.getId(), appointment.getId(),
                SessionStatus.COMPLETED, "Realizada normalmente");

        assertEquals(SessionStatus.COMPLETED, updated.getStatus());
        assertEquals("Realizada normalmente", updated.getNotes());
        assertThrows(IllegalArgumentException.class, () -> service.updateResult(
                professional.getId(), appointment.getId(), SessionStatus.NO_SHOW, null));
        verifyNoInteractions(patients);
        assertThrows(IllegalArgumentException.class, () -> service.update(
                professional.getId(), appointment.getId(), patient.getId(),
                appointment.getStartsAt(), appointment.getEndsAt(), appointment.getModality(),
                SessionStatus.CANCELED, appointment.getMeetingLink(), appointment.getNotes()));
    }

    @Test
    void resultCannotBeRecordedBeforeSessionEnds() {
        var sessions = mock(SessionAppointmentRepository.class);
        var professional = new Professional("Ana", "ana@example.com", "11999999999", null);
        var patient = new Patient(professional, "Paciente", null, "11888888888", null, PreferredContactChannel.WHATSAPP);
        var appointment = new SessionAppointment(professional, patient,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200),
                SessionModality.PRESENTIAL, null, null);
        when(sessions.findByIdAndProfessionalId(appointment.getId(), professional.getId()))
                .thenReturn(Optional.of(appointment));
        var service = new SessionAppointmentService(sessions, mock(PatientRepository.class),
                mock(ProfessionalRepository.class),mock(ScheduleBlockRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.updateResult(
                professional.getId(), appointment.getId(), SessionStatus.COMPLETED, null));
    }

    @Test
    void sessionCannotBeCreatedInsideABlockedPeriod() {
        var sessions = mock(SessionAppointmentRepository.class);
        var patients = mock(PatientRepository.class);
        var professionals = mock(ProfessionalRepository.class);
        var blocks = mock(ScheduleBlockRepository.class);
        var professional = new Professional("Ana", "ana@example.com", "11999999999", null);
        var patient = new Patient(professional, "Paciente", null, "11888888888", null, PreferredContactChannel.WHATSAPP);
        var start = Instant.now().plusSeconds(3600);
        var end = start.plusSeconds(3600);
        when(professionals.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(patients.findByIdAndProfessionalId(patient.getId(), professional.getId())).thenReturn(Optional.of(patient));
        when(blocks.existsByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThan(professional.getId(), end, start)).thenReturn(true);
        var service = new SessionAppointmentService(sessions, patients, professionals, blocks);

        assertThrows(br.com.confirmacao.session.application.SessionConflictException.class, () -> service.create(
                professional.getId(), patient.getId(), start, end, SessionModality.ONLINE, null, null));
    }

    @Test
    void confirmedSessionCannotReturnToScheduled() {
        var sessions = mock(SessionAppointmentRepository.class);
        var professional = new Professional("Ana", "ana@example.com", "11999999999", null);
        var patient = new Patient(professional, "Paciente", null, "11888888888", null, PreferredContactChannel.WHATSAPP);
        var appointment = new SessionAppointment(professional, patient, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(7200), SessionModality.ONLINE, null, null);
        appointment.confirmFromWhatsApp();
        when(sessions.findByIdAndProfessionalId(appointment.getId(), professional.getId())).thenReturn(Optional.of(appointment));
        var service = new SessionAppointmentService(sessions, mock(PatientRepository.class), mock(ProfessionalRepository.class), mock(ScheduleBlockRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.update(professional.getId(), appointment.getId(), patient.getId(), appointment.getStartsAt(), appointment.getEndsAt(), appointment.getModality(), SessionStatus.SCHEDULED, null, null));
    }

    @Test
    void expiredSessionCannotBeCanceledAsAResult() {
        var sessions = mock(SessionAppointmentRepository.class);
        var professional = new Professional("Ana", "ana@example.com", "11999999999", null);
        var patient = new Patient(professional, "Paciente", null, "11888888888", null, PreferredContactChannel.WHATSAPP);
        var appointment = new SessionAppointment(professional, patient, Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600), SessionModality.ONLINE, null, null);
        when(sessions.findByIdAndProfessionalId(appointment.getId(), professional.getId())).thenReturn(Optional.of(appointment));
        var service = new SessionAppointmentService(sessions, mock(PatientRepository.class), mock(ProfessionalRepository.class), mock(ScheduleBlockRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.updateResult(professional.getId(), appointment.getId(), SessionStatus.CANCELED, null));
    }
}
