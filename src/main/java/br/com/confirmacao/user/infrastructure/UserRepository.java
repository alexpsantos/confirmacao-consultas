package br.com.confirmacao.user.infrastructure;

import br.com.confirmacao.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    Optional<User> findByProfessionalId(UUID professionalId);

    Page<User> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndEmailAndIdNot(UUID tenantId, String email, UUID id);
}
