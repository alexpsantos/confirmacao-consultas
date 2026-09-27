package br.com.confirmacao;

import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.whatsapp.application.*;
import br.com.confirmacao.whatsapp.domain.*;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhatsAppResponseProcessorTest {
    private final WhatsAppMessageRepository messages=mock(WhatsAppMessageRepository.class);
    private final SessionAppointmentRepository sessions=mock(SessionAppointmentRepository.class);
    private final WhatsAppResponseProcessor processor=new WhatsAppResponseProcessor(messages,sessions);

    @Test void confirmsAssociatedSession() { var fixture=fixture(); stub(fixture); var result=processor.process("CONFIRM:"+fixture.message().getId()); assertTrue(result.processed()); assertEquals(WhatsAppConfirmationStatus.CONFIRMED,fixture.message().getConfirmationStatus()); assertEquals(SessionStatus.CONFIRMED,fixture.session().getStatus()); }
    @Test void cancelsAssociatedSession() { var fixture=fixture(); stub(fixture); var result=processor.process("CANCEL:"+fixture.message().getId()); assertTrue(result.processed()); assertEquals(WhatsAppConfirmationStatus.CANCELED,fixture.message().getConfirmationStatus()); assertEquals(SessionStatus.CANCELED,fixture.session().getStatus()); }
    @Test void confirmIsIdempotent() { var fixture=fixture(); stub(fixture); processor.process("CONFIRM:"+fixture.message().getId()); assertFalse(processor.process("CONFIRM:"+fixture.message().getId()).processed()); assertEquals(SessionStatus.CONFIRMED,fixture.session().getStatus()); }
    @Test void cancelIsIdempotent() { var fixture=fixture(); stub(fixture); processor.process("CANCEL:"+fixture.message().getId()); assertFalse(processor.process("CANCEL:"+fixture.message().getId()).processed()); assertEquals(SessionStatus.CANCELED,fixture.session().getStatus()); }
    @Test void rejectsConfirmAfterCancel() { var fixture=fixture(); stub(fixture); processor.process("CANCEL:"+fixture.message().getId()); assertThrows(IllegalArgumentException.class,()->processor.process("CONFIRM:"+fixture.message().getId())); }
    @Test void rejectsCancelAfterConfirm() { var fixture=fixture(); stub(fixture); processor.process("CONFIRM:"+fixture.message().getId()); assertThrows(IllegalArgumentException.class,()->processor.process("CANCEL:"+fixture.message().getId())); }
    @Test void rejectsUnknownMessageAndInvalidAction() { assertThrows(WhatsAppMessageNotFoundException.class,()->processor.process("CONFIRM:"+UUID.randomUUID())); assertThrows(IllegalArgumentException.class,()->processor.process("RESCHEDULE:"+UUID.randomUUID())); }
    @Test void rejectsCompletedOrCanceledSessionAndNeverTouchesOtherSession() { var completed=fixture(); completed.session().updateResult(SessionStatus.COMPLETED,null); stub(completed); assertThrows(IllegalArgumentException.class,()->processor.process("CONFIRM:"+completed.message().getId())); var canceled=fixture(); canceled.session().cancelFromWhatsApp(); stub(canceled); assertThrows(IllegalArgumentException.class,()->processor.process("CANCEL:"+canceled.message().getId())); var target=fixture(); var other=fixture(); stub(target); processor.process("CONFIRM:"+target.message().getId()); assertEquals(SessionStatus.SCHEDULED,other.session().getStatus()); }
    private void stub(Fixture fixture){when(messages.findById(fixture.message().getId())).thenReturn(Optional.of(fixture.message()));when(sessions.findById(fixture.session().getId())).thenReturn(Optional.of(fixture.session()));}
    private Fixture fixture(){var professional=new Professional("Profissional","p@example.com","11999999999",null);var patient=new Patient(professional,"Paciente",null,"11988888888",null,PreferredContactChannel.WHATSAPP);var session=new SessionAppointment(professional,patient,Instant.now().plusSeconds(3600),Instant.now().plusSeconds(7200),SessionModality.ONLINE,null,null);return new Fixture(session,new WhatsAppMessage(session.getId(),patient.getId(),professional.getId(),UUID.randomUUID().toString(),"consulta_confirmacao"));}
    private record Fixture(SessionAppointment session,WhatsAppMessage message) {}
}
