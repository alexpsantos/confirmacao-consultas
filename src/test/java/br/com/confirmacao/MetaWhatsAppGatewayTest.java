package br.com.confirmacao;

import br.com.confirmacao.whatsapp.application.*;
import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class MetaWhatsAppGatewayTest {
    private HttpServer server;
    @AfterEach void stop() { if(server!=null) server.stop(0); }

    @Test void rejectsIncompleteConfigurationWithoutLeakingToken() {
        var gateway=new MetaWhatsAppGateway(new WhatsAppSenderResolver(properties("http://localhost", "secret-token", "")));
        var error=assertThrows(IllegalStateException.class,()->gateway.sendConfirmation(template()));
        assertEquals("Configuração Meta do WhatsApp incompleta",error.getMessage());
        assertFalse(error.getMessage().contains("secret-token"));
    }

    @Test void sendsExpectedTemplateToMockMetaServer() throws IOException {
        var authorization=new AtomicReference<String>(); var contentType=new AtomicReference<String>(); var payload=new AtomicReference<String>();
        server=HttpServer.create(new InetSocketAddress(0),0);
        server.createContext("/v23.0/phone-id/messages",exchange->{authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));payload.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));byte[] response="{\"messages\":[{\"id\":\"wamid.test\"}]}".getBytes(StandardCharsets.UTF_8);exchange.getResponseHeaders().set("Content-Type","application/json");exchange.sendResponseHeaders(200,response.length);exchange.getResponseBody().write(response);exchange.close();}); server.start();
        var gateway=new MetaWhatsAppGateway(new WhatsAppSenderResolver(properties("http://localhost:"+server.getAddress().getPort()+"/v23.0", "secret-token", "phone-id")));
        assertEquals("wamid.test",gateway.sendConfirmation(template()));
        assertEquals("Bearer secret-token",authorization.get()); assertTrue(contentType.get().startsWith("application/json")); assertTrue(payload.get().contains("consulta_confirmacao")); assertTrue(payload.get().contains("CONFIRM:")); assertTrue(payload.get().contains("CANCEL:"));
    }

    @Test void sanitizesMetaHttpFailures() throws IOException {
        server=HttpServer.create(new InetSocketAddress(0),0); server.createContext("/v23.0/phone-id/messages",exchange->{exchange.sendResponseHeaders(401,-1);exchange.close();}); server.start();
        var gateway=new MetaWhatsAppGateway(new WhatsAppSenderResolver(properties("http://localhost:"+server.getAddress().getPort()+"/v23.0", "secret-token", "phone-id")));
        var error=assertThrows(IllegalStateException.class,()->gateway.sendConfirmation(template()));
        assertEquals("Falha ao enviar mensagem ao WhatsApp: HTTP 401",error.getMessage()); assertFalse(error.getMessage().contains("secret-token"));
    }

    private WhatsAppProperties properties(String baseUrl,String token,String phoneId) { return new WhatsAppProperties("meta",new WhatsAppProperties.Meta(baseUrl,token,phoneId,"consulta_confirmacao","pt_BR",Duration.ofSeconds(1),"","")); }
    private ConfirmationTemplate template() { return new ConfirmationTemplate(UUID.randomUUID(),"5511999999999","Paciente","Profissional","26/09/2026","14:00","CONFIRM:"+UUID.randomUUID(),"CANCEL:"+UUID.randomUUID()); }
}
