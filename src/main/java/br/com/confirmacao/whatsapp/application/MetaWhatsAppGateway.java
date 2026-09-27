package br.com.confirmacao.whatsapp.application;

import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.*;

@Component
@ConditionalOnProperty(name="app.whatsapp.provider", havingValue="meta")
public class MetaWhatsAppGateway implements WhatsAppGateway {
    private final RestClient client; private final WhatsAppProperties.Meta config;
    public MetaWhatsAppGateway(WhatsAppProperties properties) {
        config=properties.meta();
        var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(config.timeout()); factory.setReadTimeout(config.timeout());
        client=RestClient.builder().baseUrl(config.baseUrl()).requestFactory(factory).defaultHeader("Authorization", "Bearer " + config.accessToken()).build();
    }
    @Override @SuppressWarnings("unchecked") public String sendConfirmation(ConfirmationTemplate m) {
        try {
            requireConfiguration();
            Map<String,Object> body=Map.of("messaging_product","whatsapp","recipient_type","individual","to",m.to(),"type","template","template",Map.of(
                "name",config.templateName(),"language",Map.of("code",config.templateLanguage()),"components",List.of(
                    Map.of("type","body","parameters",List.of(text(m.patientName()),text(m.professionalName()),text(m.date()),text(m.time()))),
                    button("0",m.confirmPayload()), button("1",m.cancelPayload()))));
            Map<String,Object> response=client.post().uri("/{id}/messages",config.phoneNumberId()).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
            Object messages=response==null?null:response.get("messages");
            if(messages instanceof List<?> list && !list.isEmpty() && list.getFirst() instanceof Map<?,?> first && first.get("id") instanceof String id) return id;
            throw new IllegalStateException("Resposta inválida da Meta");
        } catch(RestClientResponseException e) { throw new IllegalStateException("Falha ao enviar mensagem ao WhatsApp: HTTP " + e.getStatusCode().value()); }
    }
    private Map<String,String> text(String value){return Map.of("type","text","text",value);}
    private Map<String,Object> button(String index,String payload){return Map.of("type","button","sub_type","quick_reply","index",index,"parameters",List.of(Map.of("type","payload","payload",payload)));}
    private void requireConfiguration() {
        if (blank(config.accessToken()) || blank(config.phoneNumberId()) || blank(config.templateName()) || blank(config.templateLanguage()))
            throw new IllegalStateException("Configuração Meta do WhatsApp incompleta");
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
