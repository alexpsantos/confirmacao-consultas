package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import org.springframework.stereotype.Component;
import java.util.UUID;

/** Ponto único para futuramente resolver uma conta conectada por profissional. */
@Component
public class WhatsAppSenderResolver {
    private final WhatsAppProperties properties;
    public WhatsAppSenderResolver(WhatsAppProperties properties) { this.properties=properties; }
    public WhatsAppSenderAccount resolveFor(UUID professionalId) {
        var meta=properties.meta();
        return new WhatsAppSenderAccount(meta.baseUrl(),meta.accessToken(),meta.phoneNumberId(),meta.templateName(),meta.templateLanguage(),meta.timeout());
    }
}
