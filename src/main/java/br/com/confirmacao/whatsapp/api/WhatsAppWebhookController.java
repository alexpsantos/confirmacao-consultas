package br.com.confirmacao.whatsapp.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.whatsapp.application.*;
import br.com.confirmacao.whatsapp.config.WhatsAppProperties;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/whatsapp")
public class WhatsAppWebhookController {
    private final WhatsAppResponseProcessor processor; private final WhatsAppDeliveryProcessor delivery; private final WhatsAppWebhookVerifier verifier; private final WhatsAppProperties properties; private final ObjectMapper json; private final AuditService audit;
    public WhatsAppWebhookController(WhatsAppResponseProcessor processor, WhatsAppDeliveryProcessor delivery, WhatsAppWebhookVerifier verifier, WhatsAppProperties properties, ObjectMapper json, AuditService audit) {this.processor=processor;this.delivery=delivery;this.verifier=verifier;this.properties=properties;this.json=json;this.audit=audit;}
    @GetMapping("/webhook") public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,@RequestParam("hub.verify_token") String token,@RequestParam("hub.challenge") String challenge) { return verifier.validChallenge(mode,token)?ResponseEntity.ok(challenge):ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
    @PostMapping("/webhook") public ResponseEntity<Void> webhook(@RequestBody byte[] body,@RequestHeader(value="X-Hub-Signature-256",required=false) String signature,HttpServletRequest request) {
        if(!verifier.validSignature(body,signature)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        var root=json.readTree(body);
        for(String action:actions(root)) audit(processor.process(action),request);
        deliveries(root);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/messages/{messageId}/simulate/{action}") public WhatsAppMessageResponse simulate(JwtAuthenticationToken jwt,@PathVariable UUID messageId,@PathVariable String action,HttpServletRequest request) {
        if(!"simulated".equals(properties.provider())) throw new IllegalArgumentException("Simulação disponível somente para o provedor simulated");
        var result=processor.simulate(UUID.fromString(jwt.getToken().getClaimAsString("professional_id")),messageId,action); audit(result,request); return WhatsAppMessageResponse.from(result.message());
    }
    private List<String> actions(JsonNode root) { if(root==null||!root.isObject())throw new IllegalArgumentException("Payload do WhatsApp inválido");var result=new ArrayList<String>();for(var entry:root.path("entry"))for(var change:entry.path("changes")){var value=change.path("value");for(var message:value.path("messages")){var payload=message.path("button").path("payload").asText();if(payload.isBlank())payload=message.path("interactive").path("button_reply").path("id").asText();if(!payload.isBlank())result.add(payload);}}return result; }
    private void deliveries(JsonNode root) { for(var entry:root.path("entry"))for(var change:entry.path("changes"))for(var status:change.path("value").path("statuses"))delivery.process(status.path("id").asText(null),status.path("status").asText(null)); }
    private void audit(WhatsAppActionResult result,HttpServletRequest request){if(!result.processed())return;audit.record(result.action().equals("CONFIRM")?AuditAction.WHATSAPP_CONFIRMATION_CONFIRMED:AuditAction.WHATSAPP_CONFIRMATION_CANCELED,"SESSION",result.session().getId(),request);}
}
