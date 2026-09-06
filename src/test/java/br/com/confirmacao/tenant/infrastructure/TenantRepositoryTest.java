package br.com.confirmacao.tenant.infrastructure;

import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = tenantRepository.save(
                new Tenant(
                        "Clínica Horizonte",
                        "America/Sao_Paulo"
                )
        );
    }

    @Test
    void shouldSaveTenant() {
        Optional<Tenant> result =
                tenantRepository.findById(tenant.getId());

        assertTrue(result.isPresent());
        assertEquals(
                "Clínica Horizonte",
                result.get().getDisplayName()
        );
        assertEquals(
                "America/Sao_Paulo",
                result.get().getTimezone()
        );
        assertTrue(result.get().isActive());
    }

    @Test
    void shouldListTenants() {
        tenantRepository.save(
                new Tenant(
                        "Clínica Esperança",
                        "America/Recife"
                )
        );

        List<Tenant> tenants = tenantRepository.findAll();

        assertEquals(2, tenants.size());
    }

    @Test
    void shouldReturnEmptyWhenTenantDoesNotExist() {
        tenantRepository.delete(tenant);
        tenantRepository.flush();
        entityManager.clear();

        Optional<Tenant> result =
                tenantRepository.findById(tenant.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPersistTenantUpdate() {
        tenant.update(
                "Clínica Atualizada",
                "America/Recife"
        );

        tenantRepository.flush();
        entityManager.clear();

        Tenant updatedTenant = tenantRepository
                .findById(tenant.getId())
                .orElseThrow();

        assertEquals(
                "Clínica Atualizada",
                updatedTenant.getDisplayName()
        );
        assertEquals(
                "America/Recife",
                updatedTenant.getTimezone()
        );
    }

    @Test
    void shouldPersistTenantDeactivation() {
        tenant.deactivate();

        tenantRepository.flush();
        entityManager.clear();

        Tenant deactivatedTenant = tenantRepository
                .findById(tenant.getId())
                .orElseThrow();

        assertFalse(deactivatedTenant.isActive());
    }

    @Test
    void shouldPersistTenantActivation() {
        tenant.deactivate();
        tenant.activate();

        tenantRepository.flush();
        entityManager.clear();

        Tenant activatedTenant = tenantRepository
                .findById(tenant.getId())
                .orElseThrow();

        assertTrue(activatedTenant.isActive());
    }


    @Test
    void shouldFindTenantsByActiveStatus() {
        Tenant inactiveTenant = new Tenant(
                "Clínica Inativa",
                "America/Sao_Paulo"
        );
        inactiveTenant.deactivate();

        tenantRepository.save(inactiveTenant);
        tenantRepository.flush();

        List<Tenant> activeTenants =
                tenantRepository.findAllByActive(true);

        List<Tenant> inactiveTenants =
                tenantRepository.findAllByActive(false);

        assertEquals(1, activeTenants.size());
        assertTrue(activeTenants.getFirst().isActive());
        assertEquals(tenant.getId(), activeTenants.getFirst().getId());

        assertEquals(1, inactiveTenants.size());
        assertFalse(inactiveTenants.getFirst().isActive());
        assertEquals(
                inactiveTenant.getId(),
                inactiveTenants.getFirst().getId()
        );
    }

}