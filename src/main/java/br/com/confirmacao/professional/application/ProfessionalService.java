package br.com.confirmacao.professional.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.application.TenantInactiveException;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final TenantRepository tenantRepository;

    public ProfessionalService(
            ProfessionalRepository professionalRepository,
            TenantRepository tenantRepository
    ) {
        this.professionalRepository = professionalRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Professional create(
            UUID tenantId,
            String fullName,
            String email,
            String phone,
            String registrationNumber
    ) {
        Tenant tenant = findActiveTenantOrThrow(tenantId);

        boolean emailAlreadyExists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCase(
                                tenantId,
                                email
                        );

        if (emailAlreadyExists) {
            throw new ProfessionalAlreadyExistsException(
                    "Já existe um profissional com este e-mail nesta clínica"
            );
        }

        if (registrationNumber != null && !registrationNumber.isBlank()) {
            boolean registrationAlreadyExists =
                    professionalRepository
                            .existsByTenant_IdAndRegistrationNumber(
                                    tenantId,
                                    registrationNumber
                            );

            if (registrationAlreadyExists) {
                throw new ProfessionalAlreadyExistsException(
                        "Já existe um profissional com este registro nesta clínica"
                );
            }
        }

        Professional professional = new Professional(
                tenant,
                fullName,
                email,
                phone,
                registrationNumber
        );

        return professionalRepository.save(professional);
    }

    @Transactional(readOnly = true)
    public Page<Professional> findAll(
            UUID tenantId,
            Pageable pageable
    ) {
        findActiveTenantOrThrow(tenantId);

        return professionalRepository.findAllByTenant_Id(
                tenantId,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Professional findById(
            UUID tenantId,
            UUID professionalId
    ) {
        return findProfessionalOrThrow(tenantId, professionalId);
    }

    @Transactional
    public Professional update(
            UUID tenantId,
            UUID professionalId,
            String fullName,
            String email,
            String phone,
            String registrationNumber
    ) {
        Professional professional =
                findProfessionalOrThrow(tenantId, professionalId);

        boolean emailAlreadyExists =
                professionalRepository
                        .existsByTenant_IdAndEmailIgnoreCaseAndIdNot(
                                tenantId,
                                email,
                                professionalId
                        );

        if (emailAlreadyExists) {
            throw new ProfessionalAlreadyExistsException(
                    "Já existe um profissional com este e-mail nesta clínica"
            );
        }

        if (registrationNumber != null && !registrationNumber.isBlank()) {
            boolean registrationAlreadyExists =
                    professionalRepository
                            .existsByTenant_IdAndRegistrationNumberAndIdNot(
                                    tenantId,
                                    registrationNumber,
                                    professionalId
                            );

            if (registrationAlreadyExists) {
                throw new ProfessionalAlreadyExistsException(
                        "Já existe um profissional com este registro nesta clínica"
                );
            }
        }

        professional.update(
                fullName,
                email,
                phone,
                registrationNumber
        );

        return professional;
    }

    @Transactional
    public void deactivate(
            UUID tenantId,
            UUID professionalId
    ) {
        Professional professional =
                findProfessionalOrThrow(tenantId, professionalId);

        professional.deactivate();
    }

    @Transactional
    public void activate(
            UUID tenantId,
            UUID professionalId
    ) {
        Professional professional =
                findProfessionalOrThrow(tenantId, professionalId);

        professional.activate();
    }

    private Professional findProfessionalOrThrow(
            UUID tenantId,
            UUID professionalId
    ) {
        findActiveTenantOrThrow(tenantId);

        return professionalRepository
                .findByIdAndTenant_Id(professionalId, tenantId)
                .orElseThrow(() ->
                new ProfessionalNotFoundException(professionalId)
        );
    }


    private Tenant findActiveTenantOrThrow(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new TenantNotFoundException(tenantId)
                );

        if (!tenant.isActive()) {
            throw new TenantInactiveException(tenantId);
        }

        return tenant;
    }
}