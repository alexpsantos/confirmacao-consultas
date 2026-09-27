package br.com.confirmacao.whatsapp.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@ConditionalOnProperty(name="app.whatsapp.provider", havingValue="simulated", matchIfMissing=true)
public class SimulatedWhatsAppGateway implements WhatsAppGateway {
    @Override public String sendConfirmation(ConfirmationTemplate message) { return "simulated-" + UUID.randomUUID(); }
}
