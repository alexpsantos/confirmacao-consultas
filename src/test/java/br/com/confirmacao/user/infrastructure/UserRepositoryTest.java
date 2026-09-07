package br.com.confirmacao.user.infrastructure;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private EntityManager entityManager;

    private Tenant tenant;
    private Professional professional;
    private User ownerUser;

    @BeforeEach
    void setUp() {
        tenant = tenantRepository.save(
                new Tenant(
                        "Clínica Horizonte",
                        "America/Sao_Paulo"
                )
        );

        professional = professionalRepository.save(
                new Professional(
                        tenant,
                        "Ana Souza",
                        "ana.profissional@exemplo.com",
                        "11999999999",
                        "CRP 06/123456"
                )
        );

        ownerUser = userRepository.saveAndFlush(
                new User(
                        tenant,
                        null,
                        "Alex Santos",
                        "alex@exemplo.com",
                        "password-hash",
                        UserRole.OWNER
                )
        );
    }

    @Test
    void shouldSaveUser() {
        entityManager.clear();

        User savedUser = userRepository
                .findById(ownerUser.getId())
                .orElseThrow();

        assertEquals(ownerUser.getId(), savedUser.getId());
        assertEquals(tenant.getId(), savedUser.getTenant().getId());
        assertNull(savedUser.getProfessional());
        assertEquals("Alex Santos", savedUser.getName());
        assertEquals("alex@exemplo.com", savedUser.getEmail());
        assertEquals("password-hash", savedUser.getPasswordHash());
        assertEquals(UserRole.OWNER, savedUser.getRole());
        assertTrue(savedUser.isActive());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void shouldFindUserByTenantAndEmail() {
        Optional<User> result =
                userRepository.findByTenantIdAndEmail(
                        tenant.getId(),
                        "alex@exemplo.com"
                );

        assertTrue(result.isPresent());
        assertEquals(ownerUser.getId(), result.get().getId());
    }

    @Test
    void shouldNotFindUserUsingAnotherTenant() {
        Tenant anotherTenant = tenantRepository.save(
                new Tenant(
                        "Outra clínica",
                        "America/Recife"
                )
        );

        Optional<User> result =
                userRepository.findByTenantIdAndEmail(
                        anotherTenant.getId(),
                        ownerUser.getEmail()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> result =
                userRepository.findByTenantIdAndEmail(
                        tenant.getId(),
                        "inexistente@exemplo.com"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindExistingEmailInsideTenant() {
        boolean exists =
                userRepository.existsByTenantIdAndEmail(
                        tenant.getId(),
                        ownerUser.getEmail()
                );

        assertTrue(exists);
    }

    @Test
    void shouldNotFindEmailThatDoesNotExist() {
        boolean exists =
                userRepository.existsByTenantIdAndEmail(
                        tenant.getId(),
                        "inexistente@exemplo.com"
                );

        assertFalse(exists);
    }

    @Test
    void shouldFindUserByProfessionalId() {
        User professionalUser = userRepository.saveAndFlush(
                new User(
                        tenant,
                        professional,
                        "Ana Souza",
                        "ana.login@exemplo.com",
                        "password-hash",
                        UserRole.PROFESSIONAL
                )
        );

        Optional<User> result =
                userRepository.findByProfessionalId(
                        professional.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(professionalUser.getId(), result.get().getId());
        assertEquals(
                professional.getId(),
                result.get().getProfessional().getId()
        );
    }

    @Test
    void shouldReturnEmptyWhenProfessionalHasNoUser() {
        Optional<User> result =
                userRepository.findByProfessionalId(
                        professional.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldAllowSameEmailInDifferentTenants() {
        Tenant anotherTenant = tenantRepository.save(
                new Tenant(
                        "Outra clínica",
                        "America/Recife"
                )
        );

        User anotherUser = userRepository.saveAndFlush(
                new User(
                        anotherTenant,
                        null,
                        "Outro Alex",
                        ownerUser.getEmail(),
                        "another-password-hash",
                        UserRole.OWNER
                )
        );

        assertNotNull(anotherUser.getId());

        assertTrue(
                userRepository.findByTenantIdAndEmail(
                        tenant.getId(),
                        ownerUser.getEmail()
                ).isPresent()
        );

        assertTrue(
                userRepository.findByTenantIdAndEmail(
                        anotherTenant.getId(),
                        ownerUser.getEmail()
                ).isPresent()
        );
    }

    @Test
    void shouldRejectDuplicateEmailInsideSameTenant() {
        User duplicateUser = new User(
                tenant,
                null,
                "Outro usuário",
                ownerUser.getEmail(),
                "another-password-hash",
                UserRole.ADMIN
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(duplicateUser)
        );
    }

    @Test
    void shouldRejectTwoUsersForSameProfessional() {
        userRepository.saveAndFlush(
                new User(
                        tenant,
                        professional,
                        "Ana Souza",
                        "ana.login@exemplo.com",
                        "password-hash",
                        UserRole.PROFESSIONAL
                )
        );

        User secondUser = new User(
                tenant,
                professional,
                "Outro acesso da Ana",
                "outro.login@exemplo.com",
                "another-password-hash",
                UserRole.PROFESSIONAL
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result =
                userRepository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }
}