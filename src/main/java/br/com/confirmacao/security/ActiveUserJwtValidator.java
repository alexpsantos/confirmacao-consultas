package br.com.confirmacao.security;

import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ActiveUserJwtValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INACTIVE = new OAuth2Error("invalid_token", "Usuário inativo", null);
    private final UserRepository users;

    public ActiveUserJwtValidator(UserRepository users) { this.users = users; }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            boolean active = users.findById(UUID.fromString(jwt.getSubject()))
                    .map(user -> user.isActive())
                    .orElse(false);
            return active ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(INACTIVE);
        } catch (RuntimeException exception) {
            return OAuth2TokenValidatorResult.failure(INACTIVE);
        }
    }
}
