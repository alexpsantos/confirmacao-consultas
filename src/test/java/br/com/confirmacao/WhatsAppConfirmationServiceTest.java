package br.com.confirmacao;

import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.application.SessionNotFoundException;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.whatsapp.application.*;
import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import br.com.confirmacao.whatsapp.domain.*;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WhatsAppConfirmationServiceTest {
    private final SessionAppointmentRepository sessions=mock(SessionAppointmentRepository.class);
    private final WhatsAppMessageRepository messages=mock(WhatsAppMessageRepository.class);
    private final WhatsAppGateway gateway=mock(WhatsAppGateway.class);
    private final UUID professionalId=UUID.randomUUID();
    private final WhatsAppConfirmationService service=new WhatsAppConfirmationService(sessions,messages,gateway,new WhatsAppSenderResolver(properties()));

    @Test void sendsEligibleSessionThroughSimulatedGateway() {
        var session=session(); when(sessions.findByIdAndProfessionalId(session.getId(),professionalId)).thenReturn(Optional.of(session));
        when(messages.findByIdempotencyKey(anyString())).thenReturn(Optional.empty()); when(messages.save(any())).thenAnswer(i->i.getArgument(0)); when(gateway.sendConfirmation(any())).thenReturn("simulated-1");
        var result=service.prepareConfirmation(professionalId,session.getId());
        assertEquals(WhatsAppDeliveryStatus.SENT,result.getDeliveryStatus()); assertEquals(WhatsAppConfirmationStatus.PENDING,result.getConfirmationStatus()); assertEquals(1,result.getAttemptCount());
    }
    @Test void rejectsSessionFromAnotherProfessional() {
        assertThrows(SessionNotFoundException.class,()->service.prepareConfirmation(professionalId,UUID.randomUUID())); verifyNoInteractions(gateway);
    }
    @Test void rejectsPatientWithoutValidPhone() {
        var session=mock(SessionAppointment.class); var patient=mock(Patient.class); when(session.getStatus()).thenReturn(SessionStatus.SCHEDULED); when(session.getPatient()).thenReturn(patient); when(patient.isActive()).thenReturn(true); when(patient.isWhatsappRemindersEnabled()).thenReturn(true); when(patient.getPhone()).thenReturn(null); when(sessions.findByIdAndProfessionalId(any(),eq(professionalId))).thenReturn(Optional.of(session));
        assertThrows(IllegalArgumentException.class,()->service.prepareConfirmation(professionalId,UUID.randomUUID())); verifyNoInteractions(gateway);
    }
    @Test void rejectsWhenWhatsAppAuthorizationIsDisabled() {
        var session=session(); session.getPatient().setWhatsappRemindersEnabled(false,"PROFESSIONAL"); when(sessions.findByIdAndProfessionalId(session.getId(),professionalId)).thenReturn(Optional.of(session));
        assertThrows(IllegalArgumentException.class,()->service.prepareConfirmation(professionalId,session.getId())); verifyNoInteractions(gateway);
    }
    @Test void rejectsCanceledSession() {
        var session=mock(SessionAppointment.class); when(session.getStatus()).thenReturn(SessionStatus.CANCELED); when(sessions.findByIdAndProfessionalId(any(),eq(professionalId))).thenReturn(Optional.of(session));
        assertThrows(IllegalArgumentException.class,()->service.prepareConfirmation(professionalId,UUID.randomUUID())); verifyNoInteractions(gateway);
    }
    @Test void returnsExistingMessageForDuplicateRequest() {
        var session=session(); var existing=new WhatsAppMessage(session.getId(),session.getPatient().getId(),professionalId,"existing","consulta_confirmacao"); when(sessions.findByIdAndProfessionalId(session.getId(),professionalId)).thenReturn(Optional.of(session)); when(messages.findByIdempotencyKey(anyString())).thenReturn(Optional.of(existing));
        assertSame(existing,service.prepareConfirmation(professionalId,session.getId())); verifyNoInteractions(gateway);
    }
    @Test void recordsFailureFromGateway() {
        var session=session(); when(sessions.findByIdAndProfessionalId(session.getId(),professionalId)).thenReturn(Optional.of(session)); when(messages.findByIdempotencyKey(anyString())).thenReturn(Optional.empty()); when(messages.save(any())).thenAnswer(i->i.getArgument(0)); when(gateway.sendConfirmation(any())).thenThrow(new IllegalStateException("simulated failure"));
        var result=service.prepareConfirmation(professionalId,session.getId()); assertEquals(WhatsAppDeliveryStatus.FAILED,result.getDeliveryStatus()); assertEquals(1,result.getAttemptCount());
    }
    private SessionAppointment session(){ var professional=new Professional("Profissional","pro@example.com","11999999999",null); var patient=new Patient(professional,"Paciente",null,"11988888888",null,PreferredContactChannel.WHATSAPP); return new SessionAppointment(professional,patient,Instant.now().plusSeconds(3600),Instant.now().plusSeconds(7200),SessionModality.ONLINE,null,null); }
    private WhatsAppProperties properties(){ return new WhatsAppProperties("simulated",new WhatsAppProperties.Meta("http://localhost","", "", "consulta_confirmacao","pt_BR",Duration.ofSeconds(1),"","")); }
}
