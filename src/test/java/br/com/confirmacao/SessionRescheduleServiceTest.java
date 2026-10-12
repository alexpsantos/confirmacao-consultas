package br.com.confirmacao;
import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.application.*;
import br.com.confirmacao.session.application.SessionRescheduleService.Scope;
import br.com.confirmacao.session.infrastructure.*;
import br.com.confirmacao.scheduleblock.application.ScheduleBlockService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.profiles.active=test","spring.datasource.url=jdbc:h2:mem:reschedules;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","app.security.jwt.secret=Y29uZmlybWFjYW8tY29uc3VsdGFzLXRlc3Qta2V5LTIwMjYxMjM0NTY=","app.security.jwt.issuer=test"})
class SessionRescheduleServiceTest {
 @Autowired EntityManager em;@Autowired PlatformTransactionManager transactions;@Autowired SessionRescheduleService service;@Autowired SessionAppointmentService appointments;@Autowired SessionAppointmentRepository sessions;@Autowired ScheduleBlockService blocks;
 record Fixture(UUID owner,List<UUID> ids,Instant start){}
 Fixture fixture(){return new TransactionTemplate(transactions).execute(tx->{var p=new Professional("Ana",UUID.randomUUID()+"@example.com",null,null);em.persist(p);var patient=new Patient(p,"Paciente",null,"11999999999",null,PreferredContactChannel.WHATSAPP);em.persist(patient);var seriesId=UUID.randomUUID();var start=Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES).plus(Duration.ofDays(10));var ids=new ArrayList<UUID>();for(int i=0;i<4;i++){var s=new SessionAppointment(p,patient,start.plus(Duration.ofDays(7L*i)),start.plus(Duration.ofDays(7L*i)).plusSeconds(3000),SessionModality.ONLINE,null,"Clínica");s.assignSeries(seriesId,i);em.persist(s);ids.add(s.getId());}return new Fixture(p.getId(),ids,start);});}
 SessionAppointment session(Fixture f,int index){return sessions.findByIdAndProfessionalId(f.ids().get(index),f.owner()).orElseThrow();}
 @Test void oneSessionMovesAndHistorySurvivesCommit(){var f=fixture();var old=session(f,1).getStartsAt();var start=old.plus(Duration.ofDays(1));service.reschedule(f.owner(),f.ids().get(1),start,start.plusSeconds(3000),"Viagem",Scope.ONE);assertEquals(start,session(f,1).getStartsAt());assertEquals(f.start().plus(Duration.ofDays(14)),session(f,2).getStartsAt());var history=service.history(f.owner(),f.ids().get(1));assertEquals(1,history.size());assertEquals(old,history.getFirst().getPreviousStart());assertEquals(start,history.getFirst().getNewStart());assertEquals("Viagem",history.getFirst().getReason());assertEquals("Ana",history.getFirst().getActorName());assertEquals("Clínica",session(f,1).getNotes());}
 @Test void followingMovesOnlySelectedAndLaterSeriesMembers(){var f=fixture();var start=f.start().plus(Duration.ofDays(8));var moved=service.reschedule(f.owner(),f.ids().get(1),start,start.plusSeconds(3600),"Nova rotina",Scope.FOLLOWING);assertEquals(3,moved.size());assertEquals(f.start(),session(f,0).getStartsAt());for(int i=1;i<4;i++){assertEquals(start.plus(Duration.ofDays((i-1)*7L)),session(f,i).getStartsAt());assertEquals(3600,Duration.between(session(f,i).getStartsAt(),session(f,i).getEndsAt()).getSeconds());assertEquals(1,service.history(f.owner(),f.ids().get(i)).size());}}
 @Test void conflictInLastWeekRollsBackEveryMoveAndHistory(){var f=fixture();var start=f.start().plus(Duration.ofDays(1));blocks.create(f.owner(),start.plus(Duration.ofDays(21)),start.plus(Duration.ofDays(21)).plusSeconds(3600),"Pausa");assertThrows(SessionConflictException.class,()->service.reschedule(f.owner(),f.ids().getFirst(),start,start.plusSeconds(3000),"Mudança",Scope.FOLLOWING));for(int i=0;i<4;i++){assertEquals(f.start().plus(Duration.ofDays(i*7L)),session(f,i).getStartsAt());assertTrue(service.history(f.owner(),f.ids().get(i)).isEmpty());}}
 @Test void refusesAnotherProfessionalsSessionAndHistory(){var f=fixture();assertThrows(SessionNotFoundException.class,()->service.history(UUID.randomUUID(),f.ids().getFirst()));assertThrows(SessionNotFoundException.class,()->service.reschedule(UUID.randomUUID(),f.ids().getFirst(),f.start(),f.start().plusSeconds(3000),null,Scope.ONE));}
 @Test void ordinaryEditAlsoRecordsRescheduling(){var f=fixture();var s=session(f,0);appointments.update(f.owner(),s.getId(),s.getPatient().getId(),s.getStartsAt().plusSeconds(3600),s.getEndsAt().plusSeconds(3600),s.getModality(),s.getStatus(),s.getMeetingLink(),s.getNotes());assertEquals(1,service.history(f.owner(),s.getId()).size());}
 @Test void sameDatesDoNotCreateHistory(){var f=fixture();service.reschedule(f.owner(),f.ids().getFirst(),f.start(),f.start().plusSeconds(3000),"Sem mudança",Scope.ONE);assertTrue(service.history(f.owner(),f.ids().getFirst()).isEmpty());}
 @Test void cancelFollowingPreservesEarlierSessionAndClinicalNotes(){var f=fixture();var changed=service.cancel(f.owner(),f.ids().get(1),"Viagem",Scope.FOLLOWING);assertEquals(3,changed.size());assertEquals(SessionStatus.SCHEDULED,session(f,0).getStatus());for(int i=1;i<4;i++){assertEquals(SessionStatus.CANCELED,session(f,i).getStatus());assertEquals(PaymentStatus.CANCELED,session(f,i).getPaymentStatus());assertEquals("Viagem",session(f,i).getCancellationReason());assertEquals("Clínica",session(f,i).getNotes());}}
 @Test void canceledSlotCanBeReservedAgain(){var f=fixture();service.cancel(f.owner(),f.ids().getFirst(),null,Scope.ONE);var old=session(f,0);var replacement=appointments.create(f.owner(),old.getPatient().getId(),old.getStartsAt(),old.getEndsAt(),old.getModality(),null,null);assertNotEquals(old.getId(),replacement.getId());assertEquals(SessionStatus.CANCELED,session(f,0).getStatus());assertEquals(SessionStatus.SCHEDULED,replacement.getStatus());}
}
