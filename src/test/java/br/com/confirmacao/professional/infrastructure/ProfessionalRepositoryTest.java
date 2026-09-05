package br.com.confirmacao.professional.infrastructure;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ProfessionalRepositoryTest {

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant firstTenant;
    private Tenant secondTenant;

    private Professional ana;
    private Professional bruno;
    private Professional anaFromAnotherTenant;

    @BeforeEach
    void setUp() {
        firstTenant = tenantRepository.save(
                new Tenant(
                        "Clínica Horizonte",
                        "America/Sao_Paulo"
                )
        );

        secondTenant = tenantRepository.save(
                new Tenant(
                        "Clínica Esperança",
                        "America/Sao_Paulo"
                )
        );

        ana = professionalRepository.save(
                new Professional(
                        firstTenant,
                        "Ana Souza",
                        "ana@exemplo.com",
                        "11999999999",
                        "CRP 06/123456"
                )
        );

        bruno = professionalRepository.save(
                new Professional(
                        firstTenant,
                        "Bruno Lima",
                        "bruno@exemplo.com",
                        "11988888888",
                        "CRP 06/654321"
                )
        );

        anaFromAnotherTenant = professionalRepository.save(
                new Professional(
                        secondTenant,
                        "Ana Souza",
                        "ana@exemplo.com",
                        "11977777777",
                        "CRP 06/123456"
                )
        );
    }

    @Test
    void shouldFindAllProfessionalsFromTenant() {
        List<Professional> professionals =
                professionalRepository.findAllByTenant_Id(
                        firstTenant.getId()
                );

        assertEquals(2, professionals.size());

        assertTrue(
                professionals.stream()
                        .allMatch(professional ->
                                professional.getTenant()
                                        .getId()
                                        .equals(firstTenant.getId())
                        )
        );

        assertTrue(
                professionals.stream()
                        .anyMatch(professional ->
                                professional.getId().equals(ana.getId())
                        )
        );

        assertTrue(
                professionals.stream()
                        .anyMatch(professional ->
                                professional.getId().equals(bruno.getId())
                        )
        );

        assertFalse(
                professionals.stream()
                        .anyMatch(professional ->
                                professional.getId().equals(
                                        anaFromAnotherTenant.getId()
                                )
                        )
        );
    }

    @Test
    void shouldReturnEmptyListWhenTenantHasNoProfessionals() {
        Tenant tenantWithoutProfessionals = tenantRepository.save(
                new Tenant(
                        "Clínica sem profissionais",
                        "America/Sao_Paulo"
                )
        );

        List<Professional> professionals =
                professionalRepository.findAllByTenant_Id(
                        tenantWithoutProfessionals.getId()
                );

        assertTrue(professionals.isEmpty());
    }

    @Test
    void shouldFindProfessionalByIdAndTenant() {
        Optional<Professional> result =
                professionalRepository.findByIdAndTenant_Id(
                        ana.getId(),
                        firstTenant.getId()
                );

        assertTrue(result.isPresent());
        assertEquals(ana.getId(), result.get().getId());
    }

    @Test
    void shouldNotFindProfessionalUsingAnotherTenant() {
        Optional<Professional> result =
                professionalRepository.findByIdAndTenant_Id(
                        ana.getId(),
                        secondTenant.getId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindEmailIgnoringUpperAndLowerCase() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCase(
                                firstTenant.getId(),
                                "ANA@EXEMPLO.COM"
                        );

        assertTrue(exists);
    }

    @Test
    void shouldNotFindEmailFromAnotherTenant() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCase(
                                firstTenant.getId(),
                                "outro@exemplo.com"
                        );

        assertFalse(exists);
    }

    @Test
    void shouldFindRegistrationNumberInsideTenant() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndRegistrationNumber(
                                firstTenant.getId(),
                                "CRP 06/123456"
                        );

        assertTrue(exists);
    }

    @Test
    void shouldNotFindNonexistentRegistrationNumber() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndRegistrationNumber(
                                firstTenant.getId(),
                                "CRP 06/000000"
                        );

        assertFalse(exists);
    }

    @Test
    void shouldIgnoreSameProfessionalWhenCheckingEmail() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                                firstTenant.getId(),
                                ana.getEmail(),
                                ana.getId()
                        );

        assertFalse(exists);
    }

    @Test
    void shouldFindEmailUsedByAnotherProfessional() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                                firstTenant.getId(),
                                ana.getEmail(),
                                bruno.getId()
                        );

        assertTrue(exists);
    }

    @Test
    void shouldIgnoreSameProfessionalWhenCheckingRegistration() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndRegistrationNumberAndIdNot(
                                firstTenant.getId(),
                                ana.getRegistrationNumber(),
                                ana.getId()
                        );

        assertFalse(exists);
    }

    @Test
    void shouldFindRegistrationUsedByAnotherProfessional() {
        boolean exists =
                professionalRepository
                        .existsByTenant_IdAndRegistrationNumberAndIdNot(
                                firstTenant.getId(),
                                ana.getRegistrationNumber(),
                                bruno.getId()
                        );

        assertTrue(exists);
    }
}