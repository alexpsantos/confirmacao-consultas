package br.com.confirmacao.professional.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public Optional<Professional> create(
            UUID tenantId,
            String fullName,
            String email,
            String phone,
            String registrationNumber
    ) {
        Optional<Tenant> tenantOptional = tenantRepository.findById(tenantId);

        if (tenantOptional.isEmpty()) {
            return Optional.empty();
        }

        boolean emailAlreadyExists =
                professionalRepository.existsByTenant_IdAndEmailIgnoreCase(
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
                    professionalRepository.existsByTenant_IdAndRegistrationNumber(
                            tenantId,
                            registrationNumber
                    );

            if (registrationAlreadyExists) {
                throw new ProfessionalAlreadyExistsException(
                        "Já existe um profissional com este registro nesta clínica"
                );
            }
        }

        Tenant tenant = tenantOptional.get();

        Professional professional = new Professional(
                tenant,
                fullName,
                email,
                phone,
                registrationNumber
        );

        Professional savedProfessional =
                professionalRepository.save(professional);

        return Optional.of(savedProfessional);
    }


    @Transactional(readOnly = true)
    public Optional<List<Professional>> findAll(UUID tenantId) {
        boolean tenantExists = tenantRepository.existsById(tenantId);

        if (!tenantExists) {
            return Optional.empty();
        }

        List<Professional> professionals =  professionalRepository.findAllByTenant_Id(tenantId);

        return Optional.of(professionals);
    }

    @Transactional(readOnly = true)
    public Optional<Professional> findById(UUID tenantId,  UUID professionalId) {
        return professionalRepository.findByIdAndTenant_Id(professionalId,tenantId);
    }

    @Transactional
    public Optional<Professional> update(
            UUID tenantId,
            UUID professionalId,
            String fullName,
            String email,
            String phone,
            String registrationNumber
    ) {
        Optional<Professional> professionalOptional =
                professionalRepository.findByIdAndTenant_Id(
                        professionalId,
                        tenantId
                );

        if (professionalOptional.isEmpty()) {
            return Optional.empty();
        }

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

        Professional professional = professionalOptional.get();

        professional.update(
                fullName,
                email,
                phone,
                registrationNumber
        );

        return Optional.of(professional);
    }

    @Transactional
    public boolean deactivate(UUID tenantId, UUID professionalId) {
        Optional<Professional> professionalOptional =
                professionalRepository.findByIdAndTenant_Id(
                        professionalId,
                        tenantId
                );

        if (professionalOptional.isEmpty()) {
            return false;
        }

        Professional professional = professionalOptional.get();
        professional.deactivate();

        return true;
    }

    @Transactional
    public boolean activate(UUID tenantId, UUID professionalId) {
        Optional<Professional> professionalOptional =
                professionalRepository.findByIdAndTenant_Id(
                        professionalId,
                        tenantId
                );

        if (professionalOptional.isEmpty()) {
            return false;
        }

        Professional professional = professionalOptional.get();
        professional.activate();

        return true;
    }



}
