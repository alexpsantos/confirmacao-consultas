package br.com.confirmacao.user.infrastructure;

import br.com.confirmacao.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    Optional<User> findByProfessionalId(UUID professionalId);
}