package br.com.confirmacao.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("tenantAuthorization")
public class TenantAuthorization {

    public boolean canAccess(
            Authentication authentication,
            UUID requestedTenantId
    ) {
        if (authentication == null || requestedTenantId == null) {
            return false;
        }

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        if (admin) {
            return true;
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return false;
        }

        String authenticatedTenantId = jwtAuthentication.getToken()
                .getClaimAsString("tenant_id");

        return requestedTenantId.toString().equals(authenticatedTenantId);
    }

    public boolean isProfessional(Authentication authentication, UUID professionalId) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)
                || professionalId == null) return false;
        return professionalId.toString().equals(
                jwtAuthentication.getToken().getClaimAsString("professional_id"));
    }
}
