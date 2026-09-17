package br.com.confirmacao;

import br.com.confirmacao.auth.application.AuthService;
import br.com.confirmacao.auth.application.InvalidCredentialsException;
import br.com.confirmacao.auth.application.TokenService;
import br.com.confirmacao.professional.application.ProfessionalAlreadyExistsException;
import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import br.com.confirmacao.security.ActiveUserJwtValidator;

import java.time.Instant;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminProfessionalRelationshipTest {

    private ProfessionalRepository professionals;
    private UserRepository users;
    private ProfessionalService service;
    private Professional professional;
    private User user;

    @BeforeEach
    void setUp() {
        professionals = mock(ProfessionalRepository.class);
        users = mock(UserRepository.class);
        service = new ProfessionalService(professionals, users);
        professional = new Professional("Nome original", "original@email.com", "11999999999", "CRP-1");
        user = new User(professional, "Nome original", "original@email.com", "hash", UserRole.PROFESSIONAL);
        when(professionals.findById(professional.getId())).thenReturn(Optional.of(professional));
        when(users.findByProfessionalId(professional.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void adminUpdateSynchronizesProfessionalAndLoginUser() {
        service.update(professional.getId(), "Nome atualizado", "novo@email.com", "11888888888", "CRP-2");

        assertAll(
                () -> assertEquals("Nome atualizado", professional.getFullName()),
                () -> assertEquals("novo@email.com", professional.getEmail()),
                () -> assertEquals("Nome atualizado", user.getName()),
                () -> assertEquals("novo@email.com", user.getEmail())
        );
    }

    @Test
    void duplicateUserEmailPreventsBothRecordsFromChanging() {
        when(users.existsByEmailIgnoreCaseAndIdNot("ocupado@email.com", user.getId())).thenReturn(true);

        assertThrows(ProfessionalAlreadyExistsException.class, () ->
                service.update(professional.getId(), "Outro nome", "ocupado@email.com", null, null));
        assertEquals("Nome original", professional.getFullName());
        assertEquals("Nome original", user.getName());
    }

    @Test
    void adminDeactivationDisablesProfessionalAndLoginUser() {
        service.deactivate(professional.getId());

        assertFalse(professional.isActive());
        assertFalse(user.isActive());
    }

    @Test
    void adminReactivationEnablesProfessionalAndLoginUser() {
        professional.deactivate();
        user.deactivate();

        service.activate(professional.getId());

        assertTrue(professional.isActive());
        assertTrue(user.isActive());
    }

    @Test
    void inactiveProfessionalUserCannotCreateANewSession() {
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        TokenService tokens = mock(TokenService.class);
        user.deactivate();
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () ->
                new AuthService(users, passwords, tokens).authenticate(user.getEmail(), "senha"));
        verify(tokens, never()).generate(any());
    }

    @Test
    void reactivatedProfessionalUserCanLoginAgain() {
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        TokenService tokens = mock(TokenService.class);
        user.deactivate();
        user.activate();
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(passwords.matches("senha", "hash")).thenReturn(true);
        when(tokens.generate(user)).thenReturn("jwt");

        assertEquals("jwt", new AuthService(users, passwords, tokens)
                .authenticate(user.getEmail(), "senha").accessToken());
    }

    @Test
    void tokenIssuedBeforeDeactivationIsRejectedImmediately() {
        user.deactivate();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "HS256"), java.util.Map.of("sub", user.getId().toString()));

        assertTrue(new ActiveUserJwtValidator(users).validate(jwt).hasErrors());
    }
}
