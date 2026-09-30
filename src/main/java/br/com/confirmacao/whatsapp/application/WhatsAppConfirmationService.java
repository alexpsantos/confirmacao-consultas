package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.application.SessionNotFoundException;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.whatsapp.domain.WhatsAppMessage;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class WhatsAppConfirmationService {
    private static final Logger log=LoggerFactory.getLogger(WhatsAppConfirmationService.class);
    private final SessionAppointmentRepository sessions; private final WhatsAppMessageRepository messages; private final WhatsAppGateway gateway; private final WhatsAppSenderResolver senders;
    public WhatsAppConfirmationService(SessionAppointmentRepository sessions, WhatsAppMessageRepository messages, WhatsAppGateway gateway, WhatsAppSenderResolver senders) { this.sessions=sessions; this.messages=messages; this.gateway=gateway; this.senders=senders; }
    @Transactional public WhatsAppMessage prepareConfirmation(UUID professionalId, UUID sessionId) {
        var session=sessions.findByIdAndProfessionalId(sessionId,professionalId).orElseThrow(() -> new SessionNotFoundException(sessionId));
        var patient=session.getPatient();
        if(session.getStatus()!=SessionStatus.SCHEDULED || !patient.isActive() || !patient.isWhatsappRemindersEnabled() || patient.getPhone()==null || !patient.getPhone().matches("\\d{10,15}"))
            throw new IllegalArgumentException("A sessão não está elegível para confirmação via WhatsApp");
        String key="confirmation:"+sessionId+":"+session.getUpdatedAt().toEpochMilli();
        var existing=messages.findByIdempotencyKey(key); if(existing.isPresent()) return existing.get();
        var message=messages.save(new WhatsAppMessage(sessionId,patient.getId(),professionalId,key,senders.resolveFor(professionalId).templateName()));
        var zone=ZoneId.of(session.getProfessional().getTimezone()); var start=session.getStartsAt().atZone(zone);
        log.info("WhatsApp confirmation attempt messageId={} sessionId={}",message.getId(),sessionId);
        try { var providerId=gateway.sendConfirmation(new ConfirmationTemplate(professionalId,patient.getPhone(),patient.getFullName(),session.getProfessional().getDisplayName()==null?session.getProfessional().getFullName():session.getProfessional().getDisplayName(),start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),start.format(DateTimeFormatter.ofPattern("HH:mm")),"CONFIRM:"+message.getId(),"CANCEL:"+message.getId())); message.sent(providerId); log.info("WhatsApp confirmation sent messageId={} providerMessageId={}",message.getId(),providerId); }
        catch(RuntimeException e) { message.failed("Falha ao enviar mensagem ao WhatsApp"); log.warn("WhatsApp confirmation failed messageId={} reason={}",message.getId(),safeReason(e)); }
        return message;
    }
    private String safeReason(RuntimeException error) { return error.getMessage()!=null&&error.getMessage().startsWith("Configuração Meta")?error.getMessage():"provider_error"; }
}
