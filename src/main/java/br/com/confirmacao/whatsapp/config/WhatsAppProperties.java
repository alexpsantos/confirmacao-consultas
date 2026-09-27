package br.com.confirmacao.whatsapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("app.whatsapp")
public record WhatsAppProperties(String provider, Meta meta) {
    public record Meta(String baseUrl, String accessToken, String phoneNumberId, String templateName, String templateLanguage, Duration timeout, String webhookVerifyToken, String appSecret) {}
}
