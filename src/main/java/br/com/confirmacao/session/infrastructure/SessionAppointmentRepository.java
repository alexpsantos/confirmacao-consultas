package br.com.confirmacao.session.infrastructure;
import br.com.confirmacao.session.domain.*;import org.springframework.data.jpa.repository.*;import org.springframework.data.domain.*;import java.time.Instant;import java.util.*;
public interface SessionAppointmentRepository extends JpaRepository<SessionAppointment,UUID>{
 @EntityGraph(attributePaths={"patient","professional"}) List<SessionAppointment> findAllByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(UUID professionalId,Instant end,Instant start);
 @EntityGraph(attributePaths={"patient","professional"}) Optional<SessionAppointment> findByIdAndProfessionalId(UUID id,UUID professionalId);
 @EntityGraph(attributePaths={"patient","professional"}) Page<SessionAppointment> findAllByProfessionalIdAndPatientId(UUID professionalId,UUID patientId,Pageable pageable);
 boolean existsByProfessionalIdAndPatientIdAndStatusInAndEndsAtAfter(UUID professionalId,UUID patientId,Collection<SessionStatus> statuses,Instant instant);
 boolean existsByProfessionalIdAndStatusNotAndStartsAtLessThanAndEndsAtGreaterThan(UUID professionalId,SessionStatus status,Instant end,Instant start);
 boolean existsByProfessionalIdAndStatusNotAndStartsAtLessThanAndEndsAtGreaterThanAndIdNot(UUID professionalId,SessionStatus status,Instant end,Instant start,UUID id);
}
