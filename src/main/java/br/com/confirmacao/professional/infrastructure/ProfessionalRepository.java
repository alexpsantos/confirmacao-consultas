package br.com.confirmacao.professional.infrastructure;


import br.com.confirmacao.professional.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository <Professional, UUID>{


    List<Professional> findAllByTenant_Id(UUID tenantId);
}
