package br.com.confirmacao.user.domain;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateOwnerUser() {
        Tenant tenant = createTenant();

        User user = new User(
                tenant,
                null,
                "  Alex Santos  ",
                "  ALEX@EMAIL.COM  ",
                "password-hash",
                UserRole.OWNER
        );

        assertNotNull(user.getId());
        assertEquals(tenant, user.getTenant());
        assertNull(user.getProfessional());
        assertEquals("Alex Santos", user.getName());
        assertEquals("alex@email.com", user.getEmail());
        assertEquals("password-hash", user.getPasswordHash());
        assertEquals(UserRole.OWNER, user.getRole());
        assertTrue(user.isActive());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldCreateProfessionalUser() {
        Tenant tenant = createTenant();
        Professional professional = createProfessional(tenant);

        User user = new User(
                tenant,
                professional,
                "Ana Souza",
                "ana@email.com",
                "password-hash",
                UserRole.PROFESSIONAL
        );

        assertEquals(professional, user.getProfessional());
        assertEquals(UserRole.PROFESSIONAL, user.getRole());
    }

    @Test
    void shouldRejectProfessionalUserWithoutProfessional() {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        tenant,
                        null,
                        "Ana Souza",
                        "ana@email.com",
                        "password-hash",
                        UserRole.PROFESSIONAL
                )
        );

        assertEquals(
                "Um usuário profissional deve estar vinculado a um profissional",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectProfessionalFromAnotherTenant() {
        Tenant userTenant = createTenant();
        Tenant professionalTenant = new Tenant(
                "Outra clínica",
                "America/Sao_Paulo"
        );

        Professional professional = createProfessional(professionalTenant);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        userTenant,
                        professional,
                        "Ana Souza",
                        "ana@email.com",
                        "password-hash",
                        UserRole.PROFESSIONAL
                )
        );

        assertEquals(
                "O profissional deve pertencer ao mesmo tenant do usuário",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullTenant() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        null,
                        null,
                        "Alex Santos",
                        "alex@email.com",
                        "password-hash",
                        UserRole.OWNER
                )
        );

        assertEquals("O tenant é obrigatório para usuários não administradores", exception.getMessage());
    }

    @Test
    void shouldRejectNullRole() {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        tenant,
                        null,
                        "Alex Santos",
                        "alex@email.com",
                        "password-hash",
                        null
                )
        );

        assertEquals(
                "O perfil do usuário é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullName() {
        assertInvalidName(null);
    }

    @Test
    void shouldRejectBlankName() {
        assertInvalidName("   ");
    }

    @Test
    void shouldRejectNullEmail() {
        assertInvalidEmail(null);
    }

    @Test
    void shouldRejectBlankEmail() {
        assertInvalidEmail("   ");
    }

    @Test
    void shouldRejectNullPasswordHash() {
        assertInvalidPasswordHash(null);
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertInvalidPasswordHash("   ");
    }

    @Test
    void shouldChangePasswordHash() {
        User user = createOwnerUser();

        user.changePasswordHash("new-password-hash");

        assertEquals("new-password-hash", user.getPasswordHash());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldRejectInvalidPasswordHashWhenChangingPassword() {
        User user = createOwnerUser();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> user.changePasswordHash(" ")
        );

        assertEquals(
                "A senha criptografada é obrigatória",
                exception.getMessage()
        );
    }

    @Test
    void shouldDeactivateAndActivateUser() {
        User user = createOwnerUser();

        user.deactivate();
        assertFalse(user.isActive());

        user.activate();
        assertTrue(user.isActive());
    }

    @Test
    void shouldCreateUserUsingJpaConstructor() throws Exception {
        Constructor<User> constructor =
                User.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        User user = constructor.newInstance();

        assertNotNull(user);
    }

    private void assertInvalidName(String name) {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        tenant,
                        null,
                        name,
                        "alex@email.com",
                        "password-hash",
                        UserRole.OWNER
                )
        );

        assertEquals(
                "O nome do usuário é obrigatório",
                exception.getMessage()
        );
    }

    private void assertInvalidEmail(String email) {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        tenant,
                        null,
                        "Alex Santos",
                        email,
                        "password-hash",
                        UserRole.OWNER
                )
        );

        assertEquals(
                "O e-mail do usuário é obrigatório",
                exception.getMessage()
        );
    }

    private void assertInvalidPasswordHash(String passwordHash) {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new User(
                        tenant,
                        null,
                        "Alex Santos",
                        "alex@email.com",
                        passwordHash,
                        UserRole.OWNER
                )
        );

        assertEquals(
                "A senha criptografada é obrigatória",
                exception.getMessage()
        );
    }

    private User createOwnerUser() {
        return new User(
                createTenant(),
                null,
                "Alex Santos",
                "alex@email.com",
                "password-hash",
                UserRole.OWNER
        );
    }

    private Tenant createTenant() {
        return new Tenant(
                "Clínica Teste",
                "America/Sao_Paulo"
        );
    }

    private Professional createProfessional(Tenant tenant) {
        return new Professional(
                tenant,
                "Ana Souza",
                "ana@email.com",
                "11999999999",
                "CRP 06/123456"
        );
    }
}
