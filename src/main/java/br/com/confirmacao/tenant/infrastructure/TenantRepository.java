package br.com.confirmacao.tenant.infrastructure;

import br.com.confirmacao.tenant.domain.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository
        extends JpaRepository<Tenant, UUID> {

    Page<Tenant> findAllByActive(
            boolean active,
            Pageable pageable
    );
}