package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.session.application.SessionNotFoundException;
import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.whatsapp.domain.WhatsAppConfirmationStatus;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class WhatsAppResponseProcessor {
    private final WhatsAppMessageRepository messages; private final SessionAppointmentRepository sessions;
    public WhatsAppResponseProcessor(WhatsAppMessageRepository messages, SessionAppointmentRepository sessions) { this.messages=messages; this.sessions=sessions; }
    @Transactional public WhatsAppActionResult process(String payload) {
        if(payload==null) throw new IllegalArgumentException("Ação do WhatsApp ausente");
        var parts=payload.split(":",2); if(parts.length!=2 || (!parts[0].equals("CONFIRM") && !parts[0].equals("CANCEL"))) throw new IllegalArgumentException("Ação do WhatsApp inválida");
        UUID messageId; try { messageId=UUID.fromString(parts[1]); } catch(IllegalArgumentException ex) { throw new IllegalArgumentException("Identificador de mensagem inválido"); }
        var message=messages.findById(messageId).orElseThrow(() -> new WhatsAppMessageNotFoundException(messageId));
        var session=sessions.findById(message.getSessionId()).orElseThrow(() -> new SessionNotFoundException(message.getSessionId()));
        var expected=parts[0].equals("CONFIRM")?WhatsAppConfirmationStatus.CONFIRMED:WhatsAppConfirmationStatus.CANCELED;
        if(message.getConfirmationStatus()==expected)return new WhatsAppActionResult(parts[0],message,session,false);
        if(message.getConfirmationStatus()!=WhatsAppConfirmationStatus.PENDING)throw new IllegalArgumentException("Ação conflitante para esta mensagem");
        if(parts[0].equals("CONFIRM")&&session.getStatus()!=SessionStatus.SCHEDULED)throw new IllegalArgumentException("A sessão não pode mais ser confirmada");
        if(parts[0].equals("CANCEL")&&session.getStatus()!=SessionStatus.SCHEDULED&&session.getStatus()!=SessionStatus.CONFIRMED)throw new IllegalArgumentException("A sessão não pode mais ser cancelada");
        if(parts[0].equals("CONFIRM")){message.confirm();session.confirmFromWhatsApp();}else{message.cancel();session.cancelFromWhatsApp();}
        return new WhatsAppActionResult(parts[0],message,session,true);
    }
    @Transactional public WhatsAppActionResult simulate(UUID professionalId, UUID messageId, String action) {
        var message=messages.findById(messageId).orElseThrow(() -> new WhatsAppMessageNotFoundException(messageId));
        if(!message.getProfessionalId().equals(professionalId)) throw new WhatsAppMessageNotFoundException(messageId);
        return process(action + ":" + messageId);
    }
}
