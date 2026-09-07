package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.user.domain.UserRole;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID tenantId,
        UUID professionalId,
        String name,
        UserRole role
) {

    public static LoginResponse from(AuthenticationResult result) {
        UUID professionalId = result.user().getProfessional() == null
                ? null
                : result.user().getProfessional().getId();

        return new LoginResponse(
                result.accessToken(),
                "Bearer",
                result.expiresIn(),
                result.user().getId(),
                result.user().getTenant().getId(),
                professionalId,
                result.user().getName(),
                result.user().getRole()
        );
    }
}
