package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;
import java.util.List;

public record AuthenticationResult(
        String accessToken,
        String selectionToken,
        long expiresIn,
        User user,
        List<User> memberships
) {
    public AuthenticationResult(String accessToken, long expiresIn, User user) {
        this(accessToken, null, expiresIn, user, List.of());
    }
    public static AuthenticationResult authenticated(String token, long expiresIn, User user) {
        return new AuthenticationResult(token, null, expiresIn, user, List.of());
    }

    public static AuthenticationResult selectionRequired(String token, List<User> memberships) {
        return new AuthenticationResult(null, token, 300, null, List.copyOf(memberships));
    }

    public boolean requiresTenantSelection() { return user == null; }
}
