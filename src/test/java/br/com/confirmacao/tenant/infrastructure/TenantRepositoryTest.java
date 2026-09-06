package br.com.confirmacao.tenant.infrastructure;

import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

        List<Tenant> tenants =
                tenantRepository.findAll();

        assertEquals(2, tenants.size());
    }

    @Test
    void shouldPaginateTenants() {
        tenantRepository.save(
                new Tenant(
                        "Clínica Esperança",
                        "America/Recife"
                )
        );

        Pageable pageable = PageRequest.of(0, 1);

        Page<Tenant> result =
                tenantRepository.findAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
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

        Pageable pageable = PageRequest.of(0, 10);

        Page<Tenant> activeTenants =
                tenantRepository.findAllByActive(
                        true,
                        pageable
                );

        Page<Tenant> inactiveTenants =
                tenantRepository.findAllByActive(
                        false,
                        pageable
                );

        assertEquals(
                1,
                activeTenants.getTotalElements()
        );
        assertTrue(
                activeTenants.getContent()
                        .getFirst()
                        .isActive()
        );
        assertEquals(
                tenant.getId(),
                activeTenants.getContent()
                        .getFirst()
                        .getId()
        );

        assertEquals(
                1,
                inactiveTenants.getTotalElements()
        );
        assertFalse(
                inactiveTenants.getContent()
                        .getFirst()
                        .isActive()
        );
        assertEquals(
                inactiveTenant.getId(),
                inactiveTenants.getContent()
                        .getFirst()
                        .getId()
        );
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
}