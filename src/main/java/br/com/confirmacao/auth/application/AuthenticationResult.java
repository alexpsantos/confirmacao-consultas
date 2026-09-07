package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;

public record AuthenticationResult(
        String accessToken,
        long expiresIn,
        User user
) {
}
