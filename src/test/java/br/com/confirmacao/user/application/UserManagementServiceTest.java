package br.com.confirmacao.user.application;

import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {
    @Mock UserRepository users;
    @Mock TenantRepository tenants;
    @Mock ProfessionalRepository professionals;
    @Mock PasswordEncoder encoder;
    UserManagementService service;
    Tenant tenant;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(users, tenants, professionals, encoder);
        tenant = new Tenant("Clínica", "America/Sao_Paulo");
        when(tenants.findById(tenant.getId())).thenReturn(Optional.of(tenant));
    }

    @Test
    void shouldCreateOwnerWithEncodedPassword() {
        when(users.existsByTenantIdAndEmail(tenant.getId(), "owner@exemplo.com")).thenReturn(false);
        when(encoder.encode("SenhaForte123")).thenReturn("bcrypt-hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.create(tenant.getId(), "Dono", " OWNER@EXEMPLO.COM ",
                "SenhaForte123", UserRole.OWNER, null);

        assertEquals("owner@exemplo.com", user.getEmail());
        assertEquals("bcrypt-hash", user.getPasswordHash());
        assertEquals(UserRole.OWNER, user.getRole());
    }

    @Test
    void shouldRejectDuplicateEmailInsideTenant() {
        when(users.existsByTenantIdAndEmail(tenant.getId(), "owner@exemplo.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> service.create(tenant.getId(), "Dono",
                "owner@exemplo.com", "SenhaForte123", UserRole.OWNER, null));
        verify(users, never()).save(any());
    }

    @Test
    void shouldRejectPlatformAdminRole() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.create(tenant.getId(), "Admin", "admin@exemplo.com",
                        "SenhaForte123", UserRole.ADMIN, null));
        assertEquals("O perfil ADMIN é reservado à plataforma", error.getMessage());
    }

    @Test
    void shouldNotFindUserFromAnotherTenant() {
        UUID userId = UUID.randomUUID();
        when(users.findByIdAndTenantId(userId, tenant.getId())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.findById(tenant.getId(), userId));
    }
}
