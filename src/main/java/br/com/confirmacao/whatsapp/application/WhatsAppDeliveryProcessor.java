package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.whatsapp.domain.WhatsAppDeliveryStatus;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WhatsAppDeliveryProcessor {
    private final WhatsAppMessageRepository messages;
    public WhatsAppDeliveryProcessor(WhatsAppMessageRepository messages) { this.messages=messages; }

    @Transactional
    public void process(String providerMessageId, String status) {
        if (providerMessageId == null || status == null) return;
        try { messages.findByMetaMessageId(providerMessageId).ifPresent(message -> message.updateDelivery(WhatsAppDeliveryStatus.valueOf(status.toUpperCase()))); }
        catch (IllegalArgumentException ignored) { }
    }
}
