package br.com.confirmacao.onboarding.application;

import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.application.UserManagementService;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ClinicOnboardingServiceTest {
    @Test
    void shouldCreateClinicAndItsFirstOwner() {
        TenantService tenants = mock(TenantService.class);
        UserManagementService users = mock(UserManagementService.class);
        Tenant tenant = new Tenant("Clínica Horizonte", "America/Sao_Paulo");
        User owner = new User(tenant, null, "Ana", "ana@clinica.com", "hash", UserRole.OWNER);
        when(tenants.create("Clínica Horizonte", "America/Sao_Paulo")).thenReturn(tenant);
        when(users.create(tenant.getId(), "Ana", "ana@clinica.com", "Senha@123", UserRole.OWNER, null)).thenReturn(owner);

        var result = new ClinicOnboardingService(tenants, users).onboard(
                "Clínica Horizonte", "America/Sao_Paulo", "Ana", "ana@clinica.com", "Senha@123");

        assertSame(tenant, result.tenant());
        assertSame(owner, result.owner());
        assertEquals(UserRole.OWNER, result.owner().getRole());
        verify(users).create(tenant.getId(), "Ana", "ana@clinica.com", "Senha@123", UserRole.OWNER, null);
    }
}
