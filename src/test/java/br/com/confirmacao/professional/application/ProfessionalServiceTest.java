package br.com.confirmacao.professional.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ProfessionalService professionalService;

    private UUID tenantId;
    private UUID professionalId;
    private Tenant tenant;
    private Professional professional;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        professionalId = UUID.randomUUID();

        tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        professional = new Professional(
                tenant,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );
    }

    @Test
    void shouldNotCreateProfessionalWhenTenantDoesNotExist() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        Optional<Professional> result = professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );

        assertTrue(result.isEmpty());

        verify(professionalRepository, never())
                .save(any(Professional.class));
    }

    @Test
    void shouldNotCreateProfessionalWhenEmailAlreadyExists() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCase(
                        tenantId,
                        "ana@exemplo.com"
                ))
                .thenReturn(true);

        ProfessionalAlreadyExistsException exception =
                assertThrows(
                        ProfessionalAlreadyExistsException.class,
                        () -> professionalService.create(
                                tenantId,
                                "Ana Souza",
                                "ana@exemplo.com",
                                "11999999999",
                                "CRP 06/123456"
                        )
                );

        assertEquals(
                "Já existe um profissional com este e-mail nesta clínica",
                exception.getMessage()
        );

        verify(professionalRepository, never())
                .save(any(Professional.class));
    }

    @Test
    void shouldNotCreateProfessionalWhenRegistrationAlreadyExists() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCase(
                        tenantId,
                        "ana@exemplo.com"
                ))
                .thenReturn(false);

        when(professionalRepository
                .existsByTenant_IdAndRegistrationNumber(
                        tenantId,
                        "CRP 06/123456"
                ))
                .thenReturn(true);

        ProfessionalAlreadyExistsException exception =
                assertThrows(
                        ProfessionalAlreadyExistsException.class,
                        () -> professionalService.create(
                                tenantId,
                                "Ana Souza",
                                "ana@exemplo.com",
                                "11999999999",
                                "CRP 06/123456"
                        )
                );

        assertEquals(
                "Já existe um profissional com este registro nesta clínica",
                exception.getMessage()
        );

        verify(professionalRepository, never())
                .save(any(Professional.class));
    }

    @Test
    void shouldCreateProfessionalWhenDataIsValid() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCase(
                        tenantId,
                        "ana@exemplo.com"
                ))
                .thenReturn(false);

        when(professionalRepository
                .existsByTenant_IdAndRegistrationNumber(
                        tenantId,
                        "CRP 06/123456"
                ))
                .thenReturn(false);

        when(professionalRepository.save(any(Professional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Professional> result = professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );

        assertTrue(result.isPresent());
        assertEquals("Ana Souza", result.get().getFullName());
        assertEquals("ana@exemplo.com", result.get().getEmail());
        assertEquals("CRP 06/123456", result.get().getRegistrationNumber());

        verify(professionalRepository)
                .save(any(Professional.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldCreateProfessionalWithoutRegistration(
            String registrationNumber
    ) {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCase(
                        tenantId,
                        "ana@exemplo.com"
                ))
                .thenReturn(false);

        when(professionalRepository.save(any(Professional.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Professional> result = professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                registrationNumber
        );

        assertTrue(result.isPresent());

        verify(professionalRepository, never())
                .existsByTenant_IdAndRegistrationNumber(
                        tenantId,
                        registrationNumber
                );
    }

    @Test
    void shouldReturnEmptyWhenListingNonexistentTenant() {
        when(tenantRepository.existsById(tenantId))
                .thenReturn(false);

        Optional<List<Professional>> result =
                professionalService.findAll(tenantId);

        assertTrue(result.isEmpty());

        verify(professionalRepository, never())
                .findAllByTenant_Id(tenantId);
    }

    @Test
    void shouldListProfessionalsFromTenant() {
        when(tenantRepository.existsById(tenantId))
                .thenReturn(true);

        when(professionalRepository.findAllByTenant_Id(tenantId))
                .thenReturn(List.of(professional));

        Optional<List<Professional>> result =
                professionalService.findAll(tenantId);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertSame(professional, result.get().getFirst());
    }

    @Test
    void shouldFindProfessionalByIdAndTenant() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        Optional<Professional> result =
                professionalService.findById(
                        tenantId,
                        professionalId
                );

        assertTrue(result.isPresent());
        assertSame(professional, result.get());
    }

    @Test
    void shouldNotUpdateProfessionalWhenProfessionalDoesNotExist() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        Optional<Professional> result =
                professionalService.update(
                        tenantId,
                        professionalId,
                        "Ana Atualizada",
                        "atualizada@exemplo.com",
                        "11988887777",
                        "CRP 06/654321"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotUpdateProfessionalWhenEmailAlreadyExists() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                        tenantId,
                        "duplicado@exemplo.com",
                        professionalId
                ))
                .thenReturn(true);

        ProfessionalAlreadyExistsException exception =
                assertThrows(
                        ProfessionalAlreadyExistsException.class,
                        () -> professionalService.update(
                                tenantId,
                                professionalId,
                                "Ana Souza",
                                "duplicado@exemplo.com",
                                "11999999999",
                                "CRP 06/123456"
                        )
                );

        assertEquals(
                "Já existe um profissional com este e-mail nesta clínica",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotUpdateProfessionalWhenRegistrationAlreadyExists() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                        tenantId,
                        "ana@exemplo.com",
                        professionalId
                ))
                .thenReturn(false);

        when(professionalRepository
                .existsByTenant_IdAndRegistrationNumberAndIdNot(
                        tenantId,
                        "CRP 06/999999",
                        professionalId
                ))
                .thenReturn(true);

        ProfessionalAlreadyExistsException exception =
                assertThrows(
                        ProfessionalAlreadyExistsException.class,
                        () -> professionalService.update(
                                tenantId,
                                professionalId,
                                "Ana Souza",
                                "ana@exemplo.com",
                                "11999999999",
                                "CRP 06/999999"
                        )
                );

        assertEquals(
                "Já existe um profissional com este registro nesta clínica",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateProfessionalWhenDataIsValid() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                        tenantId,
                        "atualizada@exemplo.com",
                        professionalId
                ))
                .thenReturn(false);

        when(professionalRepository
                .existsByTenant_IdAndRegistrationNumberAndIdNot(
                        tenantId,
                        "CRP 06/654321",
                        professionalId
                ))
                .thenReturn(false);

        Optional<Professional> result =
                professionalService.update(
                        tenantId,
                        professionalId,
                        "Ana Atualizada",
                        "atualizada@exemplo.com",
                        "11988887777",
                        "CRP 06/654321"
                );

        assertTrue(result.isPresent());
        assertEquals("Ana Atualizada", result.get().getFullName());
        assertEquals("atualizada@exemplo.com", result.get().getEmail());
        assertEquals("11988887777", result.get().getPhone());
        assertEquals("CRP 06/654321", result.get().getRegistrationNumber());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldUpdateProfessionalWithoutRegistration(
            String registrationNumber
    ) {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        when(professionalRepository
                .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                        tenantId,
                        "atualizada@exemplo.com",
                        professionalId
                ))
                .thenReturn(false);

        Optional<Professional> result =
                professionalService.update(
                        tenantId,
                        professionalId,
                        "Ana Atualizada",
                        "atualizada@exemplo.com",
                        "11988887777",
                        registrationNumber
                );

        assertTrue(result.isPresent());

        verify(professionalRepository, never())
                .existsByTenant_IdAndRegistrationNumberAndIdNot(
                        tenantId,
                        registrationNumber,
                        professionalId
                );
    }

    @Test
    void shouldReturnFalseWhenDeactivatingNonexistentProfessional() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        boolean result =
                professionalService.deactivate(
                        tenantId,
                        professionalId
                );

        assertFalse(result);
    }

    @Test
    void shouldDeactivateProfessional() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        boolean result =
                professionalService.deactivate(
                        tenantId,
                        professionalId
                );

        assertTrue(result);
        assertFalse(professional.isActive());
    }

    @Test
    void shouldReturnFalseWhenActivatingNonexistentProfessional() {
        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        boolean result =
                professionalService.activate(
                        tenantId,
                        professionalId
                );

        assertFalse(result);
    }

    @Test
    void shouldActivateProfessional() {
        professional.deactivate();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        boolean result =
                professionalService.activate(
                        tenantId,
                        professionalId
                );

        assertTrue(result);
        assertTrue(professional.isActive());
    }
}