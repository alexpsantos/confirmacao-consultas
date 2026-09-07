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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private AuthService authService;
    private Tenant tenant;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                tokenService
        );

        tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        user = new User(
                tenant,
                null,
                "Alex Santos",
                "alex@exemplo.com",
                "password-hash",
                UserRole.OWNER
        );
    }

    @Test
    void shouldAuthenticateUser() {
        when(userRepository.findByTenantIdAndEmail(
                tenant.getId(),
                user.getEmail()
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                user.getPasswordHash()
        )).thenReturn(true);

        when(tokenService.generate(user))
                .thenReturn("jwt-token");
        when(tokenService.expirationSeconds())
                .thenReturn(28_800L);

        AuthenticationResult result = authService.authenticate(
                tenant.getId(),
                "  ALEX@EXEMPLO.COM  ",
                "correct-password"
        );

        assertEquals("jwt-token", result.accessToken());
        assertEquals(28_800L, result.expiresIn());
        assertSame(user, result.user());

        verify(userRepository).findByTenantIdAndEmail(
                tenant.getId(),
                "alex@exemplo.com"
        );
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByTenantIdAndEmail(
                tenant.getId(),
                "unknown@exemplo.com"
        )).thenReturn(Optional.empty());

        assertInvalidCredentials(() -> authService.authenticate(
                tenant.getId(),
                "unknown@exemplo.com",
                "any-password"
        ));

        verifyNoInteractions(passwordEncoder, tokenService);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        mockUserFound();
        when(passwordEncoder.matches(
                "incorrect-password",
                user.getPasswordHash()
        )).thenReturn(false);

        assertInvalidCredentials(() -> authService.authenticate(
                tenant.getId(),
                user.getEmail(),
                "incorrect-password"
        ));

        verifyNoInteractions(tokenService);
    }

    @Test
    void shouldRejectInactiveUser() {
        user.deactivate();
        mockUserFound();

        assertInvalidCredentials(() -> authService.authenticate(
                tenant.getId(),
                user.getEmail(),
                "correct-password"
        ));

        verifyNoInteractions(passwordEncoder, tokenService);
    }

    @Test
    void shouldRejectUserFromInactiveTenant() {
        tenant.deactivate();
        mockUserFound();

        assertInvalidCredentials(() -> authService.authenticate(
                tenant.getId(),
                user.getEmail(),
                "correct-password"
        ));

        verifyNoInteractions(passwordEncoder, tokenService);
    }

    private void mockUserFound() {
        when(userRepository.findByTenantIdAndEmail(
                tenant.getId(),
                user.getEmail()
        )).thenReturn(Optional.of(user));
    }

    private void assertInvalidCredentials(Runnable action) {
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                action::run
        );

        assertEquals(
                "E-mail ou senha inválidos",
                exception.getMessage()
        );
    }
}
