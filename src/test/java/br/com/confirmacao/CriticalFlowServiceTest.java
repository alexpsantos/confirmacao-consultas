package br.com.confirmacao;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.audit.domain.AuditLog;
import br.com.confirmacao.audit.infrastructure.AuditLogRepository;
import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.auth.application.TokenService;
import br.com.confirmacao.patient.application.PatientNotFoundException;
import br.com.confirmacao.patient.application.PatientService;
import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.patient.domain.PreferredContactChannel;
import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.scheduleblock.infrastructure.ScheduleBlockRepository;
import br.com.confirmacao.session.application.SessionAppointmentService;
import br.com.confirmacao.session.application.SessionConflictException;
import br.com.confirmacao.session.domain.SessionModality;
import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CriticalFlowServiceTest {

    @Test
    void loginRejectsWrongPasswordWithoutGeneratingToken() {
        var users = mock(UserRepository.class);
        var passwords = mock(PasswordEncoder.class);
        var tokens = mock(TokenService.class);
        var professional = new Professional("Ana", "ana@example.com", null, null);
        var user = new User(professional, "Ana", "ana@example.com", "hash", UserRole.PROFESSIONAL);
        when(users.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        when(passwords.matches("senha-errada", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> new AuthService(users, passwords, tokens).authenticate("ANA@EXAMPLE.COM", "senha-errada"));

        verify(tokens, never()).generate(any());
    }

    @Test
    void patientSessionsCannotBeReadByAnotherProfessional() {
        var patients = mock(PatientRepository.class);
        var service = new PatientService(patients, mock(ProfessionalRepository.class), mock(SessionAppointmentRepository.class));
        var authenticatedProfessional = UUID.randomUUID();
        var patientFromAnotherProfessional = UUID.randomUUID();

        when(patients.findByIdAndProfessionalId(patientFromAnotherProfessional, authenticatedProfessional))
                .thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,
                () -> service.findById(authenticatedProfessional, patientFromAnotherProfessional));
    }

    @Test
    void patientListIsAlwaysScopedToAuthenticatedProfessional() {
        var patients = mock(PatientRepository.class);
        var service = new PatientService(patients, mock(ProfessionalRepository.class), mock(SessionAppointmentRepository.class));
        var professionalId = UUID.randomUUID();
        var page = PageRequest.of(0, 20);

        service.findAll(professionalId, page);

        verify(patients).findAllByProfessionalId(professionalId, page);
    }

    @Test
    void overlappingSessionIsRejected() {
        var sessions = mock(SessionAppointmentRepository.class);
        var patients = mock(PatientRepository.class);
        var professionals = mock(ProfessionalRepository.class);
        var blocks = mock(ScheduleBlockRepository.class);
        var professional = new Professional("Ana", "ana@example.com", null, null);
        var patient = new Patient(professional, "João", null, "11999999999", null, PreferredContactChannel.WHATSAPP);
        var start = Instant.now().plusSeconds(7200);
        var end = start.plusSeconds(3600);
        when(professionals.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(patients.findByIdAndProfessionalId(patient.getId(), professional.getId())).thenReturn(Optional.of(patient));
        when(sessions.existsByProfessionalIdAndStatusNotAndStartsAtLessThanAndEndsAtGreaterThan(
                professional.getId(), SessionStatus.CANCELED, end, start)).thenReturn(true);

        var service = new SessionAppointmentService(sessions, patients, professionals, blocks);

        assertThrows(SessionConflictException.class,
                () -> service.create(professional.getId(), patient.getId(), start, end, SessionModality.ONLINE, null, null));
    }

    @Test
    void auditStoresActionResourceAndForwardedAddress() {
        var logs = mock(AuditLogRepository.class);
        var request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        var resourceId = UUID.randomUUID();

        new AuditService(logs).record(AuditAction.SESSION_CREATED, "SESSION", resourceId, request);

        var captured = ArgumentCaptor.forClass(AuditLog.class);
        verify(logs).save(captured.capture());
        assertEquals(AuditAction.SESSION_CREATED, captured.getValue().getAction());
        assertEquals("SESSION", captured.getValue().getResourceType());
        assertEquals(resourceId, captured.getValue().getResourceId());
        assertEquals("203.0.113.10", captured.getValue().getIpAddress());
    }
}
