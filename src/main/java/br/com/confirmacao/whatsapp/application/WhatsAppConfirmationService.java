package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.application.SessionNotFoundException;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import br.com.confirmacao.whatsapp.domain.WhatsAppMessage;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class WhatsAppConfirmationService {
    private final SessionAppointmentRepository sessions; private final WhatsAppMessageRepository messages; private final WhatsAppGateway gateway; private final WhatsAppProperties properties;
    public WhatsAppConfirmationService(SessionAppointmentRepository sessions, WhatsAppMessageRepository messages, WhatsAppGateway gateway, WhatsAppProperties properties) { this.sessions=sessions; this.messages=messages; this.gateway=gateway; this.properties=properties; }
    @Transactional public WhatsAppMessage prepareConfirmation(UUID professionalId, UUID sessionId) {
        var session=sessions.findByIdAndProfessionalId(sessionId,professionalId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var patient=session.getPatient();
        if(session.getStatus()!=SessionStatus.SCHEDULED || !patient.isActive() || !patient.isWhatsappRemindersEnabled() || patient.getPhone()==null || !patient.getPhone().matches("\\d{10,15}"))
            throw new IllegalArgumentException("A sessão não está elegível para confirmação via WhatsApp");
        String key="confirmation:"+sessionId+":"+session.getUpdatedAt().toEpochMilli();
        var existing=messages.findByIdempotencyKey(key); if(existing.isPresent()) return existing.get();
        var message=messages.save(new WhatsAppMessage(sessionId,patient.getId(),professionalId,key,properties.meta().templateName()));
        var zone=ZoneId.of(session.getProfessional().getTimezone()); var start=session.getStartsAt().atZone(zone);
        try { message.sent(gateway.sendConfirmation(new ConfirmationTemplate(patient.getPhone(),patient.getFullName(),session.getProfessional().getDisplayName()==null?session.getProfessional().getFullName():session.getProfessional().getDisplayName(),start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),start.format(DateTimeFormatter.ofPattern("HH:mm")),"CONFIRM:"+message.getId(),"CANCEL:"+message.getId()))); }
        catch(RuntimeException e) { message.failed(e.getMessage()); }
        return message;
    }
}
