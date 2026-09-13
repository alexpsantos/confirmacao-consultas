package br.com.confirmacao.onboarding.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.auth.api.LoginResponse;
import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.auth.application.TokenService;
import br.com.confirmacao.onboarding.application.ClinicOnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class ClinicOnboardingController {
    private final ClinicOnboardingService onboarding;
    private final TokenService tokens;
    private final AuditService audit;

    public ClinicOnboardingController(ClinicOnboardingService onboarding,
                                      TokenService tokens, AuditService audit) {
        this.onboarding = onboarding;
        this.tokens = tokens;
        this.audit = audit;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> onboard(@Valid @RequestBody ClinicOnboardingRequest request,
                                                  HttpServletRequest httpRequest) {
        var result = onboarding.onboard(request.clinicName(), request.timezone(),
                request.ownerName(), request.ownerEmail(), request.password());
        audit.recordAuthenticated(AuditAction.TENANT_CREATED, result.tenant().getId(),
                "TENANT", result.tenant().getId(), httpRequest);
        audit.recordAuthenticated(AuditAction.USER_CREATED, result.tenant().getId(),
                "USER", result.owner().getId(), httpRequest);
        var authentication = AuthenticationResult.authenticated(
                tokens.generate(result.owner()), tokens.expirationSeconds(), result.owner());
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse.from(authentication));
    }
}
