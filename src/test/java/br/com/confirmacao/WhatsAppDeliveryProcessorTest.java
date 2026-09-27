package br.com.confirmacao;

import br.com.confirmacao.whatsapp.application.WhatsAppDeliveryProcessor;
import br.com.confirmacao.whatsapp.domain.*;
import br.com.confirmacao.whatsapp.infrastructure.WhatsAppMessageRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WhatsAppDeliveryProcessorTest {
    private final WhatsAppMessageRepository messages=mock(WhatsAppMessageRepository.class);
    private final WhatsAppDeliveryProcessor processor=new WhatsAppDeliveryProcessor(messages);

    @Test void updatesKnownProviderMessageWithoutChangingConfirmation() {
        var message=new WhatsAppMessage(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),"key","consulta_confirmacao");
        message.sent("wamid.1"); when(messages.findByMetaMessageId("wamid.1")).thenReturn(Optional.of(message));
        processor.process("wamid.1","delivered");
        assertEquals(WhatsAppDeliveryStatus.DELIVERED,message.getDeliveryStatus());
        assertEquals(WhatsAppConfirmationStatus.PENDING,message.getConfirmationStatus());
    }

    @Test void ignoresUnknownOrUnsupportedDeliveryEvents() {
        when(messages.findByMetaMessageId("wamid.unknown")).thenReturn(Optional.empty());
        processor.process("wamid.unknown","read"); processor.process("wamid.unknown","unsupported");
        verify(messages, times(2)).findByMetaMessageId("wamid.unknown");
    }
}
