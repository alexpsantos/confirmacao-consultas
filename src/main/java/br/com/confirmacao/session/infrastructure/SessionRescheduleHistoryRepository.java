package br.com.confirmacao.session.infrastructure;
import br.com.confirmacao.session.domain.SessionRescheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SessionRescheduleHistoryRepository extends JpaRepository<SessionRescheduleHistory,UUID>{List<SessionRescheduleHistory> findAllBySessionIdAndSessionProfessionalIdOrderByChangedAtDescIdDesc(UUID sessionId,UUID professionalId);}
