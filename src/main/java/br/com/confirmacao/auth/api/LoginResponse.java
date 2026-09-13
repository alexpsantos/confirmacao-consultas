package br.com.confirmacao.auth.api;

import br.com.confirmacao.auth.application.AuthenticationResult;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import java.util.List;
import java.util.UUID;

public record LoginResponse(
        boolean requiresTenantSelection,
        String selectionToken,
        List<TenantOptionResponse> tenants,
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        UUID tenantId,
        String tenantName,
        UUID professionalId,
        String name,
        UserRole role
) {
    public static LoginResponse from(AuthenticationResult result) {
        if (result.requiresTenantSelection()) {
            return new LoginResponse(true, result.selectionToken(),
                    result.memberships().stream().map(TenantOptionResponse::from).toList(),
                    null, null, result.expiresIn(), null, null, null, null, null, null);
        }
        User user = result.user();
        return new LoginResponse(false, null, List.of(), result.accessToken(), "Bearer", result.expiresIn(),
                user.getId(), user.getTenant() == null ? null : user.getTenant().getId(),
                user.getTenant() == null ? null : user.getTenant().getDisplayName(),
                user.getProfessional() == null ? null : user.getProfessional().getId(),
                user.getName(), user.getRole());
    }
}
