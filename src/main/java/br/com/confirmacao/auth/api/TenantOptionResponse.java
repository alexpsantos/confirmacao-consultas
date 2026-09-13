package br.com.confirmacao.auth.api;

import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import java.util.UUID;

public record TenantOptionResponse(UUID id, String displayName, UserRole role) {
    public static TenantOptionResponse from(User user) {
        return new TenantOptionResponse(user.getTenant().getId(), user.getTenant().getDisplayName(), user.getRole());
    }
}
