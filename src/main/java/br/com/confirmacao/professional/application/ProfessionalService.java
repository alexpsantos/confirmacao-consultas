package br.com.confirmacao.professional.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Tenant tenant = tenantOptional.get();

        Professional professional = new Professional(
                tenant,
                fullName,
                email,
                phone,
                registrationNumber
        );

        Professional savedProfessional = professionalRepository.save(professional);
        return Optional.of(savedProfessional);
    }



}
