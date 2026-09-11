package br.com.confirmacao.user.api;

import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, UUID tenantId, UUID professionalId, String name, String email,
                           UserRole role, boolean active, Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getTenant().getId(),
                user.getProfessional() == null ? null : user.getProfessional().getId(),
                user.getName(), user.getEmail(), user.getRole(), user.isActive(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
