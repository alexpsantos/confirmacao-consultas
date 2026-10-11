package br.com.confirmacao.admin.api;

import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.domain.SessionStatus;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/operational-summary")
public class AdminOperationalSummaryController {
    private final ProfessionalRepository professionals;
    private final PatientRepository patients;
    private final SessionAppointmentRepository sessions;

    public AdminOperationalSummaryController(ProfessionalRepository professionals, PatientRepository patients, SessionAppointmentRepository sessions) {
        this.professionals = professionals;
        this.patients = patients;
        this.sessions = sessions;
    }

    @GetMapping
    public AdminOperationalSummaryResponse summary(@RequestParam Instant start, @RequestParam Instant end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("O período informado é inválido");
        return new AdminOperationalSummaryResponse(
                professionals.countByActiveTrue(),
                patients.countByActiveTrue(),
                sessions.countByStartsAtGreaterThanEqualAndStartsAtLessThan(start, end),
                sessions.countByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatus(start, end, SessionStatus.CANCELED)
        );
    }
}
