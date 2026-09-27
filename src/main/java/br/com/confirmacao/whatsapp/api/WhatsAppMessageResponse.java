package br.com.confirmacao.whatsapp.api;
import br.com.confirmacao.whatsapp.domain.*;
import java.time.Instant;
import java.util.UUID;
public record WhatsAppMessageResponse(UUID id, UUID sessionId, String providerMessageId, WhatsAppDeliveryStatus deliveryStatus, WhatsAppConfirmationStatus confirmationStatus, int attemptCount, Instant lastAttemptAt) {
    public static WhatsAppMessageResponse from(WhatsAppMessage message) { return new WhatsAppMessageResponse(message.getId(), message.getSessionId(), message.getMetaMessageId(), message.getDeliveryStatus(), message.getConfirmationStatus(), message.getAttemptCount(), message.getLastAttemptAt()); }
}
