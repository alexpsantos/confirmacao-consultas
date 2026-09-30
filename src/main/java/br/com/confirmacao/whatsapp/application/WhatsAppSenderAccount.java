package br.com.confirmacao.whatsapp.application;

import java.time.Duration;

/** Conta remetente atual. Sem toString para não expor o token em logs acidentais. */
public final class WhatsAppSenderAccount {
    private final String baseUrl, accessToken, phoneNumberId, templateName, templateLanguage;
    private final Duration timeout;
    public WhatsAppSenderAccount(String baseUrl, String accessToken, String phoneNumberId, String templateName, String templateLanguage, Duration timeout) { this.baseUrl=baseUrl; this.accessToken=accessToken; this.phoneNumberId=phoneNumberId; this.templateName=templateName; this.templateLanguage=templateLanguage; this.timeout=timeout; }
    public String baseUrl(){ return baseUrl; } public String accessToken(){ return accessToken; } public String phoneNumberId(){ return phoneNumberId; } public String templateName(){ return templateName; } public String templateLanguage(){ return templateLanguage; } public Duration timeout(){ return timeout; }
}
