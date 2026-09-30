package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.whatsapp.domain.WhatsAppDeliveryStatus;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WhatsAppDeliveryProcessor {
    private static final Logger log=LoggerFactory.getLogger(WhatsAppDeliveryProcessor.class);
    private final WhatsAppMessageRepository messages;
    public WhatsAppDeliveryProcessor(WhatsAppMessageRepository messages) { this.messages=messages; }

    @Transactional
    public void process(String providerMessageId, String status) {
        if (providerMessageId == null || status == null) return;
        try { messages.findByMetaMessageId(providerMessageId).ifPresent(message -> { if(message.updateDelivery(WhatsAppDeliveryStatus.valueOf(status.toUpperCase()))) log.info("WhatsApp delivery updated providerMessageId={} status={}",providerMessageId,status); }); }
        catch (IllegalArgumentException ignored) { }
    }
}
