package br.com.confirmacao.tenant.application;

import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );
    }

    @Test
    void shouldCreateTenant() {
        when(tenantRepository.save(any(Tenant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Tenant result = tenantService.create(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        assertEquals(
                "Clínica Horizonte",
                result.getDisplayName()
        );
        assertEquals(
                "America/Sao_Paulo",
                result.getTimezone()
        );
        assertTrue(result.isActive());

        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void shouldListTenants() {
        when(tenantRepository.findAll())
                .thenReturn(List.of(tenant));

        List<Tenant> result = tenantService.findAll();

        assertEquals(1, result.size());
        assertSame(tenant, result.getFirst());
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoTenants() {
        when(tenantRepository.findAll())
                .thenReturn(List.of());

        List<Tenant> result = tenantService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindTenantById() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        Tenant result = tenantService.findById(tenantId);

        assertSame(tenant, result);
    }

    @Test
    void shouldThrowWhenTenantDoesNotExist() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        TenantNotFoundException exception = assertThrows(
                TenantNotFoundException.class,
                () -> tenantService.findById(tenantId)
        );

        assertEquals(
                "Clínica não encontrada: " + tenantId,
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        Tenant result = tenantService.update(
                tenantId,
                "Clínica Atualizada",
                "America/Recife"
        );

        assertSame(tenant, result);
        assertEquals(
                "Clínica Atualizada",
                result.getDisplayName()
        );
        assertEquals(
                "America/Recife",
                result.getTimezone()
        );
    }

    @Test
    void shouldThrowWhenUpdatingNonexistentTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                TenantNotFoundException.class,
                () -> tenantService.update(
                        tenantId,
                        "Clínica Atualizada",
                        "America/Recife"
                )
        );
    }

    @Test
    void shouldDeactivateTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        tenantService.deactivate(tenantId);

        assertFalse(tenant.isActive());
    }

    @Test
    void shouldThrowWhenDeactivatingNonexistentTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                TenantNotFoundException.class,
                () -> tenantService.deactivate(tenantId)
        );
    }

    @Test
    void shouldActivateTenant() {
        tenant.deactivate();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        tenantService.activate(tenantId);

        assertTrue(tenant.isActive());
    }

    @Test
    void shouldThrowWhenActivatingNonexistentTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                TenantNotFoundException.class,
                () -> tenantService.activate(tenantId)
        );
    }
}