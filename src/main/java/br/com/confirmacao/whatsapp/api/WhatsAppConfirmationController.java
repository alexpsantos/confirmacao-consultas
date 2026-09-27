package br.com.confirmacao.whatsapp.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.whatsapp.application.WhatsAppConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/whatsapp-confirmation")
public class WhatsAppConfirmationController {
    private final WhatsAppConfirmationService service; private final AuditService audit;
    public WhatsAppConfirmationController(WhatsAppConfirmationService service, AuditService audit) { this.service=service; this.audit=audit; }
    @PostMapping public ResponseEntity<WhatsAppMessageResponse> request(JwtAuthenticationToken jwt, @PathVariable UUID sessionId, HttpServletRequest request) {
        var message=service.prepareConfirmation(UUID.fromString(jwt.getToken().getClaimAsString("professional_id")), sessionId);
        audit.record(AuditAction.WHATSAPP_CONFIRMATION_REQUESTED, "WHATSAPP_MESSAGE", message.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(WhatsAppMessageResponse.from(message));
    }
}
