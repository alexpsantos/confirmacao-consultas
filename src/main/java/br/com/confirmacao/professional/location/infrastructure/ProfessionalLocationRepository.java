package br.com.confirmacao.professional.location.infrastructure;
import br.com.confirmacao.professional.location.domain.ProfessionalLocation;import org.springframework.data.jpa.repository.JpaRepository;import java.util.UUID;
public interface ProfessionalLocationRepository extends JpaRepository<ProfessionalLocation,UUID>{}
