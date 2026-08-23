package br.com.confirmacao.tenant.infrastructure;

import br.com.confirmacao.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {


}
