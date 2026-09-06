package br.com.confirmacao.professional.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

import br.com.confirmacao.tenant.application.TenantInactiveException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verifyNoInteractions;


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

        TenantNotFoundException exception = assertThrows(
                TenantNotFoundException.class,
                () -> professionalService.create(
                        tenantId,
                        "Ana Souza",
                        "ana@exemplo.com",
                        "11999999999",
                        "CRP 06/123456"
                )
        );

        assertEquals(
                "Clínica não encontrada: " + tenantId,
                exception.getMessage()
        );

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

        Professional result = professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );

        assertEquals("Ana Souza", result.getFullName());
        assertEquals("ana@exemplo.com", result.getEmail());
        assertEquals(
                "CRP 06/123456",
                result.getRegistrationNumber()
        );

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

        Professional result = professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                registrationNumber
        );

        assertEquals(
                registrationNumber,
                result.getRegistrationNumber()
        );

        verify(professionalRepository, never())
                .existsByTenant_IdAndRegistrationNumber(
                        tenantId,
                        registrationNumber
                );
    }

    @Test
    void shouldThrowWhenListingNonexistentTenant() {
        Pageable pageable = PageRequest.of(0, 20);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        TenantNotFoundException exception = assertThrows(
                TenantNotFoundException.class,
                () -> professionalService.findAll(tenantId, pageable)
        );

        assertEquals(
                "Clínica não encontrada: " + tenantId,
                exception.getMessage()
        );

        verify(professionalRepository, never())
                .findAllByTenant_Id(
                        tenantId,
                        pageable
                );
    }

    @Test
    void shouldListProfessionalsFromTenant() {
        mockActiveTenant();

        Pageable pageable = PageRequest.of(0, 2);

        Page<Professional> repositoryResult = new PageImpl<>(
                List.of(professional),
                pageable,
                1
        );

        when(professionalRepository.findAllByTenant_Id(
                tenantId,
                pageable
        )).thenReturn(repositoryResult);

        Page<Professional> result =
                professionalService.findAll(tenantId, pageable);

        assertEquals(1, result.getContent().size());
        assertSame(
                professional,
                result.getContent().getFirst()
        );
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void shouldFindProfessionalByIdAndTenant() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        Professional result =
                professionalService.findById(
                        tenantId,
                        professionalId
                );

        assertSame(professional, result);
    }

    @Test
    void shouldThrowWhenFindingNonexistentProfessional() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        ProfessionalNotFoundException exception = assertThrows(
                ProfessionalNotFoundException.class,
                () -> professionalService.findById(
                        tenantId,
                        professionalId
                )
        );

        assertEquals(
                "Profissional não encontrado nesta clínica: "
                        + professionalId,
                exception.getMessage()
        );
    }

    @Test
    void shouldNotUpdateProfessionalWhenProfessionalDoesNotExist() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        ProfessionalNotFoundException exception = assertThrows(
                ProfessionalNotFoundException.class,
                () -> professionalService.update(
                        tenantId,
                        professionalId,
                        "Ana Atualizada",
                        "atualizada@exemplo.com",
                        "11988887777",
                        "CRP 06/654321"
                )
        );

        assertEquals(
                "Profissional não encontrado nesta clínica: "
                        + professionalId,
                exception.getMessage()
        );
    }

    @Test
    void shouldNotUpdateProfessionalWhenEmailAlreadyExists() {
        mockActiveTenant();

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
        mockActiveTenant();

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
        mockActiveTenant();

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

        Professional result = professionalService.update(
                tenantId,
                professionalId,
                "Ana Atualizada",
                "atualizada@exemplo.com",
                "11988887777",
                "CRP 06/654321"
        );

        assertEquals("Ana Atualizada", result.getFullName());
        assertEquals(
                "atualizada@exemplo.com",
                result.getEmail()
        );
        assertEquals("11988887777", result.getPhone());
        assertEquals(
                "CRP 06/654321",
                result.getRegistrationNumber()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldUpdateProfessionalWithoutRegistration(
            String registrationNumber
    ) {
        mockActiveTenant();

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

        Professional result = professionalService.update(
                tenantId,
                professionalId,
                "Ana Atualizada",
                "atualizada@exemplo.com",
                "11988887777",
                registrationNumber
        );

        assertEquals(
                registrationNumber,
                result.getRegistrationNumber()
        );

        verify(professionalRepository, never())
                .existsByTenant_IdAndRegistrationNumberAndIdNot(
                        tenantId,
                        registrationNumber,
                        professionalId
                );
    }

    @Test
    void shouldThrowWhenDeactivatingNonexistentProfessional() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                ProfessionalNotFoundException.class,
                () -> professionalService.deactivate(
                        tenantId,
                        professionalId
                )
        );
    }

    @Test
    void shouldDeactivateProfessional() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        professionalService.deactivate(
                tenantId,
                professionalId
        );

        assertFalse(professional.isActive());
    }

    @Test
    void shouldThrowWhenActivatingNonexistentProfessional() {
        mockActiveTenant();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                ProfessionalNotFoundException.class,
                () -> professionalService.activate(
                        tenantId,
                        professionalId
                )
        );
    }

    @Test
    void shouldActivateProfessional() {
        mockActiveTenant();

        professional.deactivate();

        when(professionalRepository.findByIdAndTenant_Id(
                professionalId,
                tenantId
        )).thenReturn(Optional.of(professional));

        professionalService.activate(
                tenantId,
                professionalId
        );

        assertTrue(professional.isActive());
    }

    @Test
    void shouldBlockAllOperationsWhenTenantIsInactive() {
        tenant.deactivate();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        assertAll(
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.create(
                                tenantId,
                                "Ana Souza",
                                "nova.ana@exemplo.com",
                                "11999999999",
                                "CRP 06/999999"
                        )
                ),
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.findAll(
                                tenantId,
                                PageRequest.of(0, 20)
                        )
                ),
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.findById(
                                tenantId,
                                professionalId
                        )
                ),
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.update(
                                tenantId,
                                professionalId,
                                "Ana Atualizada",
                                "atualizada@exemplo.com",
                                "11988887777",
                                "CRP 06/654321"
                        )
                ),
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.deactivate(
                                tenantId,
                                professionalId
                        )
                ),
                () -> assertThrows(
                        TenantInactiveException.class,
                        () -> professionalService.activate(
                                tenantId,
                                professionalId
                        )
                )
        );

        verifyNoInteractions(professionalRepository);
    }

    private void mockActiveTenant() {
        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));
    }
}
