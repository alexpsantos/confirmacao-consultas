package br.com.confirmacao.professional.infrastructure;


import br.com.confirmacao.professional.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ProfessionalRepository extends JpaRepository <Professional, UUID>{


    List<Professional> findAllByTenant_Id(UUID tenantId);

    Optional<Professional> findByIdAndTenant_Id(UUID professionalId,UUID tenantId);

    boolean existsByTenant_IdAndEmailIgnoreCase(UUID tenantId, String email);

    boolean existsByTenant_IdAndRegistrationNumber(UUID tenantId, String registrationNumber);

    boolean existsByTenant_IdAndEmailIgnoreCaseAndIdNot(UUID tenantId,String email,UUID professionalId);

    boolean existsByTenant_IdAndRegistrationNumberAndIdNot(UUID tenantId,String registrationNumber,UUID professionalId);



}

