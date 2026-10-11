package br.com.confirmacao.homologation;

import br.com.confirmacao.patient.application.PatientService;
import br.com.confirmacao.patient.domain.PreferredContactChannel;
import br.com.confirmacao.professional.access.ProfessionalAccessService;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.application.SessionAppointmentService;
import br.com.confirmacao.session.domain.SessionModality;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Profile("homolog")
@ConditionalOnProperty(prefix = "app.homolog", name = "seed", havingValue = "true", matchIfMissing = true)
public class HomologationDataInitializer implements ApplicationRunner {
    private static final String EMAIL = "profissional.homolog@confirma.test";

    private final ProfessionalRepository professionals;
    private final ProfessionalAccessService access;
    private final PatientService patients;
    private final SessionAppointmentService sessions;

    public HomologationDataInitializer(ProfessionalRepository professionals, ProfessionalAccessService access,
                                       PatientService patients, SessionAppointmentService sessions) {
        this.professionals = professionals;
        this.access = access;
        this.patients = patients;
        this.sessions = sessions;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (professionals.existsByEmailIgnoreCase(EMAIL)) return;

        var professional = access.create("Profissional de Homologação", EMAIL, "11999990000", null,
                "America/Sao_Paulo", "Homologacao123").professional();
        var ana = patients.create(professional.getId(), "Paciente Teste Ana", LocalDate.of(1990, 1, 15),
                "11999990001", "ana.teste@confirma.test", PreferredContactChannel.WHATSAPP, true);
        var bruno = patients.create(professional.getId(), "Paciente Teste Bruno", LocalDate.of(1988, 7, 20),
                "11999990002", "bruno.teste@confirma.test", PreferredContactChannel.WHATSAPP, true);
        var zone = ZoneId.of("America/Sao_Paulo");
        var first = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant();
        var second = first.plusSeconds(60 * 60);
        sessions.create(professional.getId(), ana.getId(), first, first.plusSeconds(50 * 60), SessionModality.ONLINE, null, "Sessão fictícia de homologação");
        sessions.create(professional.getId(), bruno.getId(), second, second.plusSeconds(50 * 60), SessionModality.PRESENTIAL, null, "Sessão fictícia de homologação");
    }
}
