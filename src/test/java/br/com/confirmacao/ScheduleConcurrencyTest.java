package br.com.confirmacao;

import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.application.*;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.scheduleblock.application.ScheduleBlockService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.profiles.active=test","spring.datasource.url=jdbc:h2:mem:scheduleconcurrency;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","app.security.jwt.secret=Y29uZmlybWFjYW8tY29uc3VsdGFzLXRlc3Qta2V5LTIwMjYxMjM0NTY=","app.security.jwt.issuer=test"})
class ScheduleConcurrencyTest {
 @Autowired EntityManager em;
 @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
 @Autowired PlatformTransactionManager transactions;
 @Autowired SessionAppointmentService sessions;
 @Autowired SessionSeriesService series;
 @Autowired ScheduleBlockService blocks;
 @Autowired ProfessionalRepository professionals;
 @Autowired SessionAppointmentRepository repository;
 record Fixture(UUID professionalId,UUID patientId,Instant start){}
 Fixture fixture(){jdbc.execute("ALTER TABLE patients ADD COLUMN IF NOT EXISTS default_session_weekday INTEGER");return new TransactionTemplate(transactions).execute(tx->{var p=new Professional("Ana",UUID.randomUUID()+"@example.com",null,null);em.persist(p);var patient=new Patient(p,"Paciente",null,"11999999999",null,PreferredContactChannel.WHATSAPP);em.persist(patient);return new Fixture(p.getId(),patient.getId(),Instant.now().plus(Duration.ofDays(10)));});}
 void create(Fixture f,Instant start){sessions.create(f.professionalId(),f.patientId(),start,start.plusSeconds(3000),SessionModality.ONLINE,null,null);}
 void conflict(Runnable first,Runnable second) throws Exception {
  var held=new CountDownLatch(1);var release=new CountDownLatch(1);var entered=new CountDownLatch(1);var workers=Executors.newFixedThreadPool(2);
  try {
   var winner=workers.submit(()->new TransactionTemplate(transactions).executeWithoutResult(tx->{first.run();em.flush();held.countDown();try{if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Timed out waiting for test");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}));
   if(!held.await(10,TimeUnit.SECONDS)){winner.get(1,TimeUnit.SECONDS);fail("First transaction did not acquire reservation");}
   var loser=workers.submit(()->{entered.countDown();second.run();});assertTrue(entered.await(5,TimeUnit.SECONDS));
   assertThrows(TimeoutException.class,()->loser.get(200,TimeUnit.MILLISECONDS),"Competing reservation must wait for first transaction");
   release.countDown();winner.get(10,TimeUnit.SECONDS);
   var error=assertThrows(ExecutionException.class,()->loser.get(10,TimeUnit.SECONDS));assertInstanceOf(SessionConflictException.class,error.getCause());
  } finally {release.countDown();workers.shutdownNow();assertTrue(workers.awaitTermination(10,TimeUnit.SECONDS));}
 }
 @Test void concurrentSessionsReserveOnlyOnce() throws Exception {var f=fixture();conflict(()->create(f,f.start()),()->create(f,f.start()));assertEquals(1,repository.findAllByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(f.professionalId(),f.start().plusSeconds(3600),f.start()).size());}
 @Test void blockPreventsConcurrentSession() throws Exception {var f=fixture();conflict(()->blocks.create(f.professionalId(),f.start(),f.start().plusSeconds(3600),"Pausa"),()->create(f,f.start()));}
 @Test void sessionPreventsConcurrentBlock() throws Exception {var f=fixture();conflict(()->create(f,f.start()),()->blocks.create(f.professionalId(),f.start(),f.start().plusSeconds(3600),"Pausa"));}
 @Test void seriesPreventsConcurrentSessionInLaterWeek() throws Exception {var f=fixture();conflict(()->series.create(f.professionalId(),f.patientId(),f.start(),f.start().plusSeconds(3000),SessionModality.ONLINE,null,null),()->create(f,f.start().plus(Duration.ofDays(14))));assertEquals(4,repository.findAllByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(f.professionalId(),f.start().plus(Duration.ofDays(22)),f.start()).size());}
 @Test void reschedulingPreventsConcurrentReservationOfTarget() throws Exception {var f=fixture();var existing=sessions.create(f.professionalId(),f.patientId(),f.start().plus(Duration.ofDays(1)),f.start().plus(Duration.ofDays(1)).plusSeconds(3000),SessionModality.ONLINE,null,null);conflict(()->sessions.update(f.professionalId(),existing.getId(),f.patientId(),f.start(),f.start().plusSeconds(3000),SessionModality.ONLINE,SessionStatus.SCHEDULED,null,null),()->create(f,f.start()));}
 @Test void anotherProfessionalCanReserveWithoutWaiting() throws Exception {var a=fixture();var b=fixture();var held=new CountDownLatch(1);var release=new CountDownLatch(1);var workers=Executors.newFixedThreadPool(2);try{var first=workers.submit(()->new TransactionTemplate(transactions).executeWithoutResult(tx->{professionals.lockSchedule(a.professionalId());held.countDown();try{release.await(10,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}));assertTrue(held.await(10,TimeUnit.SECONDS));workers.submit(()->create(b,a.start())).get(5,TimeUnit.SECONDS);release.countDown();first.get(10,TimeUnit.SECONDS);}finally{release.countDown();workers.shutdownNow();workers.awaitTermination(10,TimeUnit.SECONDS);}}
}
