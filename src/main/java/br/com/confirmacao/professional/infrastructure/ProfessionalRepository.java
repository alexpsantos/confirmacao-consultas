package br.com.confirmacao.professional.infrastructure;


import br.com.confirmacao.professional.domain.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository <Professional, UUID>{


}
