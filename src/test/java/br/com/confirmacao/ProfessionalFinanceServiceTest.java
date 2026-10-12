package br.com.confirmacao;
import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.application.ProfessionalFinanceService;
import br.com.confirmacao.session.application.ProfessionalFinanceService.DateBasis;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.profiles.active=test","spring.datasource.url=jdbc:h2:mem:professionalfinance;MODE=PostgreSQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","app.security.jwt.secret=Y29uZmlybWFjYW8tY29uc3VsdGFzLXRlc3Qta2V5LTIwMjYxMjM0NTY=","app.security.jwt.issuer=test"})
class ProfessionalFinanceServiceTest {
 static final java.util.concurrent.atomic.AtomicLong phones=new java.util.concurrent.atomic.AtomicLong(11999000000L);
 @Autowired EntityManager em;@Autowired PlatformTransactionManager transactions;@Autowired ProfessionalFinanceService service;
 record Fixture(UUID owner,UUID patient,UUID otherPatient){}
 Fixture fixture(){return new TransactionTemplate(transactions).execute(tx->{var p=new Professional("Ana",UUID.randomUUID()+"@example.com",null,null);p.updateProfile("Ana",p.getEmail(),null,null,null,null,"America/Rio_Branco",50,"ONLINE",true);em.persist(p);var a=patient(p);var b=patient(p);for(int i=0;i<25;i++)entry(p,a,"2026-10-14T12:00:00Z",100,PaymentStatus.PAID,PaymentMethod.PIX,"2026-10-12");entry(p,b,"2026-10-14T12:00:00Z",120,PaymentStatus.PENDING,null,null);entry(p,b,"2026-10-14T13:00:00Z",120,PaymentStatus.PENDING,null,null);entry(p,a,"2026-10-14T14:00:00Z",50,PaymentStatus.EXEMPT,null,null);entry(p,a,"2026-10-14T15:00:00Z",90,PaymentStatus.CANCELED,null,null);var other=new Professional("Outra",UUID.randomUUID()+"@example.com",null,null);em.persist(other);entry(other,patient(other),"2026-10-14T12:00:00Z",900,PaymentStatus.PAID,PaymentMethod.CASH,"2026-10-12");return new Fixture(p.getId(),a.getId(),b.getId());});}
 Patient patient(Professional p){var a=new Patient(p,"Paciente",null,String.valueOf(phones.incrementAndGet()),null,PreferredContactChannel.WHATSAPP);em.persist(a);return a;}
 void entry(Professional p,Patient a,String start,int amount,PaymentStatus status,PaymentMethod method,String paymentDate){var instant=Instant.parse(start);var s=new SessionAppointment(p,a,instant,instant.plusSeconds(3000),SessionModality.ONLINE,null,null);s.updateFinance(new BigDecimal(amount),status,paymentDate==null?null:LocalDate.parse(paymentDate),null);s.updatePaymentMethod(method);if(status==PaymentStatus.CANCELED)s.cancelFromWhatsApp();em.persist(s);}
 void money(String expected,BigDecimal actual){assertEquals(0,new BigDecimal(expected).compareTo(actual));}
 @Test void totalsCoverAllPagesAndExcludeCanceledAndExempt(){var f=fixture();var result=service.find(f.owner(),null,null,null,DateBasis.SESSION,null,null,false,0,20);assertEquals(20,result.entries().content().size());assertEquals(29,result.entries().totalElements());money("2500",result.totals().received());money("240",result.totals().pending());money("50",result.totals().exempt());money("2740",result.totals().total());var next=service.find(f.owner(),null,null,null,DateBasis.SESSION,null,null,false,1,20);assertEquals(9,next.entries().content().size());money("2740",next.totals().total());}
 @Test void filtersPatientStatusAndMethodInTotalsAndEntries(){var f=fixture();var pending=service.find(f.owner(),f.otherPatient(),null,null,DateBasis.SESSION,PaymentStatus.PENDING,null,false,0,20);assertEquals(2,pending.entries().totalElements());money("240",pending.totals().total());var pix=service.find(f.owner(),f.patient(),null,null,DateBasis.SESSION,PaymentStatus.PAID,PaymentMethod.PIX,false,0,20);assertEquals(25,pix.entries().totalElements());money("2500",pix.totals().received());var cash=service.find(f.owner(),null,null,null,DateBasis.SESSION,null,PaymentMethod.CASH,false,0,20);assertEquals(0,cash.entries().totalElements());money("0",cash.totals().total());}
 @Test void paymentPeriodExcludesUndatedPendingEntries(){var f=fixture();var result=service.find(f.owner(),null,LocalDate.parse("2026-10-12"),LocalDate.parse("2026-10-12"),DateBasis.PAYMENT,null,null,false,0,20);assertEquals(25,result.entries().totalElements());money("2500",result.totals().received());money("0",result.totals().pending());}
 @Test void sessionPeriodUsesProfessionalTimezoneAndIncludesEntireLastDay(){var f=fixture();new TransactionTemplate(transactions).executeWithoutResult(tx->{var p=em.find(Professional.class,f.owner());var a=em.find(Patient.class,f.patient());entry(p,a,"2026-10-01T04:00:00Z",10,PaymentStatus.PENDING,null,null);entry(p,a,"2026-10-02T04:59:00Z",20,PaymentStatus.PENDING,null,null);entry(p,a,"2026-10-02T05:00:00Z",30,PaymentStatus.PENDING,null,null);});var result=service.find(f.owner(),null,LocalDate.parse("2026-10-01"),LocalDate.parse("2026-10-01"),DateBasis.SESSION,null,null,false,0,20);assertEquals(1,result.entries().totalElements());money("20",result.totals().pending());}
 @Test void rejectsInvalidPeriodAndPagination(){var f=fixture();assertThrows(IllegalArgumentException.class,()->service.find(f.owner(),null,LocalDate.parse("2026-10-20"),LocalDate.parse("2026-10-01"),DateBasis.SESSION,null,null,false,0,20));assertThrows(IllegalArgumentException.class,()->service.find(f.owner(),null,null,null,DateBasis.SESSION,null,null,false,0,101));}
}
