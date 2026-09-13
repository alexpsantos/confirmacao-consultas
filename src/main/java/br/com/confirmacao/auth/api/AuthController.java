package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.auth.application.PasswordResetService;
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
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, AuditService auditService,
                          PasswordResetService passwordResetService) {
        this.authService = authService;
        this.auditService = auditService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            AuthenticationResult result = authService.authenticate(
                    request.email(), request.password());

            if (!result.requiresTenantSelection()) {
                auditService.recordLogin(AuditAction.LOGIN_SUCCESS,
                        result.user().getTenant() == null ? null : result.user().getTenant().getId(),
                        result.user(), httpRequest);
            }

            return ResponseEntity.ok(LoginResponse.from(result));
        } catch (InvalidCredentialsException exception) {
            auditService.recordLogin(AuditAction.LOGIN_FAILURE,
                    null, null, httpRequest);
            throw exception;
        }
    }

    @PostMapping("/select-tenant")
    public ResponseEntity<LoginResponse> selectTenant(
            @Valid @RequestBody SelectTenantRequest request,
            HttpServletRequest httpRequest) {
        AuthenticationResult result = authService.selectTenant(request.selectionToken(), request.tenantId());
        auditService.recordLogin(AuditAction.LOGIN_SUCCESS, request.tenantId(), result.user(), httpRequest);
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ForgotPasswordResponse> requestPasswordReset(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        var result = passwordResetService.request(request.tenantId(), request.email());
        if (result.user() != null) {
            auditService.recordPasswordReset(AuditAction.PASSWORD_RESET_REQUESTED,
                    result.user(), httpRequest);
        }
        return ResponseEntity.accepted().body(new ForgotPasswordResponse(
                "Se os dados estiverem corretos, você receberá as instruções de recuperação",
                result.token()));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        var user = passwordResetService.reset(request.token(), request.newPassword());
        auditService.recordPasswordReset(AuditAction.PASSWORD_RESET_COMPLETED,
                user, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
