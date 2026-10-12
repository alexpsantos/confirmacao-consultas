package br.com.confirmacao;
import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.application.SessionAppointmentService;
import br.com.confirmacao.session.api.SessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.profiles.active=test","spring.datasource.url=jdbc:h2:mem:paymentmethod;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","app.security.jwt.secret=Y29uZmlybWFjYW8tY29uc3VsdGFzLXRlc3Qta2V5LTIwMjYxMjM0NTY=","app.security.jwt.issuer=test"})
class SessionPaymentMethodPersistenceTest {
 @Autowired EntityManager em;
 @Autowired PlatformTransactionManager transactions;
 @Autowired SessionAppointmentService service;
 @Test void pixSurvivesCommitAndReload(){
  var tx=new TransactionTemplate(transactions);
  var appointment=tx.execute(state->{var p=new Professional("Ana","pix@example.com",null,null);em.persist(p);var patient=new Patient(p,"Paciente",null,"11999999999",null,PreferredContactChannel.WHATSAPP);em.persist(patient);var s=new SessionAppointment(p,patient,Instant.now().plusSeconds(3600),Instant.now().plusSeconds(7200),SessionModality.ONLINE,null,null);em.persist(s);return s;});
  var response=tx.execute(state->SessionResponse.from(service.updateFinance(appointment.getProfessional().getId(),appointment.getId(),new BigDecimal("100.00"),PaymentStatus.PAID,LocalDate.now(),"Pix recebido",PaymentMethod.PIX)));
  assertEquals(PaymentMethod.PIX,response.paymentMethod());
  tx.executeWithoutResult(state->{em.clear();var saved=em.find(SessionAppointment.class,appointment.getId());assertEquals(PaymentMethod.PIX,saved.getPaymentMethod());assertEquals("Pix recebido",saved.getFinancialNotes());});
 }
}
