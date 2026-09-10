package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditService auditService;

    public AuthController(AuthService authService, AuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            AuthenticationResult result = authService.authenticate(
                    request.tenantId(), request.email(), request.password());

            auditService.recordLogin(AuditAction.LOGIN_SUCCESS,
                    request.tenantId(), result.user(), httpRequest);

            return ResponseEntity.ok(LoginResponse.from(result));
        } catch (InvalidCredentialsException exception) {
            auditService.recordLogin(AuditAction.LOGIN_FAILURE,
                    request.tenantId(), null, httpRequest);
            throw exception;
        }
    }
}
