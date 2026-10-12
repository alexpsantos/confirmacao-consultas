package br.com.confirmacao.professional.infrastructure;import br.com.confirmacao.professional.domain.Professional;import org.springframework.data.jpa.repository.JpaRepository;import java.util.UUID;public interface ProfessionalRepository extends JpaRepository<Professional,UUID>{
/** Serializes availability checks and reservations for one professional until transaction commit. */
@org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
@org.springframework.data.jpa.repository.Query("select p from Professional p where p.id = :professionalId")
java.util.List<Professional> lockSchedule(@org.springframework.data.repository.query.Param("professionalId") UUID professionalId);
boolean existsByEmailIgnoreCase(String email);boolean existsByRegistrationNumber(String registrationNumber);boolean existsByEmailIgnoreCaseAndIdNot(String email,UUID id);boolean existsByRegistrationNumberAndIdNot(String registrationNumber,UUID id);long countByActiveTrue();}
