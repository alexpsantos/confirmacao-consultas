package br.com.confirmacao.auth.application;

import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthMultiTenantTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder passwords;
    @Mock TokenService tokens;
    @Mock TenantSelectionTokenService selectionTokens;
    AuthService service;

    @BeforeEach void setUp() { service = new AuthService(users, passwords, tokens, selectionTokens); }

    @Test
    void shouldSelectOnlyActiveTenantAutomatically() {
        User user = membership("Clínica Única");
        when(users.findAllByEmailIgnoreCase("alex@exemplo.com")).thenReturn(List.of(user));
        when(passwords.matches("senha", "hash")).thenReturn(true);
        when(tokens.generate(user)).thenReturn("jwt-final");
        when(tokens.expirationSeconds()).thenReturn(28_800L);

        AuthenticationResult result = service.authenticate(" ALEX@EXEMPLO.COM ", "senha");

        assertFalse(result.requiresTenantSelection());
        assertEquals("jwt-final", result.accessToken());
        assertEquals(user, result.user());
    }

    @Test
    void shouldRequireSelectionWhenUserHasMultipleActiveTenants() {
        User first = membership("Clínica A");
        User second = membership("Clínica B");
        when(users.findAllByEmailIgnoreCase("alex@exemplo.com")).thenReturn(List.of(first, second));
        when(passwords.matches("senha", "hash")).thenReturn(true);
        when(selectionTokens.generate("alex@exemplo.com", List.of(first, second))).thenReturn("jwt-temporario");

        AuthenticationResult result = service.authenticate("alex@exemplo.com", "senha");

        assertTrue(result.requiresTenantSelection());
        assertEquals("jwt-temporario", result.selectionToken());
        assertEquals(2, result.memberships().size());
        verify(tokens, never()).generate(any());
    }

    private User membership(String tenantName) {
        return new User(new Tenant(tenantName, "America/Sao_Paulo"), null,
                "Alex", "alex@exemplo.com", "hash", UserRole.OWNER);
    }
}
