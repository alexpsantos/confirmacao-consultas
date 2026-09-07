package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.auth.application.AuthenticationResult;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthenticationResult result = authService.authenticate(
                request.tenantId(),
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
