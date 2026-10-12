package br.com.confirmacao;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.patient.domain.*;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.application.*;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.patient.infrastructure.PatientRepository;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.scheduleblock.infrastructure.ScheduleBlockRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SessionFinanceTest {
 private SessionAppointment appointment(){var p=new Professional("Ana","ana@example.com",null,null);var patient=new Patient(p,"Paciente",null,"11999999999",null,PreferredContactChannel.WHATSAPP);return new SessionAppointment(p,patient,Instant.now().plusSeconds(3600),Instant.now().plusSeconds(7200),SessionModality.ONLINE,null,null);}
 @Test void newSessionCopiesProfessionalPriceAndResultDoesNotMarkItPaid(){var s=appointment();assertEquals(s.getProfessional().getDefaultSessionPrice(),s.getFinancialAmount());s.updateResult(SessionStatus.COMPLETED,null);assertEquals(PaymentStatus.PENDING,s.getPaymentStatus());}
 @Test void financialEditIsIndependentOfAppointmentAndAllowsCompletedSessions(){var s=appointment();s.updateResult(SessionStatus.COMPLETED,"Clínica");s.updateFinance(new BigDecimal("175.50"),PaymentStatus.PAID,LocalDate.now()," Pix ");assertEquals(new BigDecimal("175.50"),s.getFinancialAmount());assertEquals("Pix",s.getFinancialNotes());assertEquals("Clínica",s.getNotes());assertEquals(SessionStatus.COMPLETED,s.getStatus());assertEquals(new BigDecimal("100.00"),s.getProfessional().getDefaultSessionPrice());}
 @Test void cancellationByWhatsAppClearsPaymentAndPreventsBillableStatus(){var s=appointment();s.updateFinance(new BigDecimal("120"),PaymentStatus.PAID,LocalDate.now(),null);s.cancelFromWhatsApp();assertEquals(PaymentStatus.CANCELED,s.getPaymentStatus());assertNull(s.getPaymentDate());assertThrows(IllegalArgumentException.class,()->s.updateFinance(BigDecimal.TEN,PaymentStatus.PENDING,null,null));s.updateFinance(BigDecimal.TEN,PaymentStatus.EXEMPT,null,null);assertEquals(PaymentStatus.EXEMPT,s.getPaymentStatus());}
 @Test void agendaCancellationPreservesExemption(){var s=appointment();s.updateFinance(BigDecimal.TEN,PaymentStatus.EXEMPT,null,"Cortesia");s.update(s.getPatient(),s.getStartsAt(),s.getEndsAt(),s.getModality(),SessionStatus.CANCELED,null,null);assertEquals(PaymentStatus.EXEMPT,s.getPaymentStatus());assertEquals("Cortesia",s.getFinancialNotes());}
 @Test void validatesAmountAndPaymentDate(){var s=appointment();assertThrows(IllegalArgumentException.class,()->s.updateFinance(new BigDecimal("-1"),PaymentStatus.PENDING,null,null));assertThrows(IllegalArgumentException.class,()->s.updateFinance(new BigDecimal("1.001"),PaymentStatus.PENDING,null,null));assertThrows(IllegalArgumentException.class,()->s.updateFinance(BigDecimal.TEN,PaymentStatus.PAID,null,null));assertThrows(IllegalArgumentException.class,()->s.updateFinance(BigDecimal.TEN,PaymentStatus.PENDING,LocalDate.now(),null));}
 @Test void financeLookupIsScopedToAuthenticatedProfessional(){var s=appointment();var repo=mock(SessionAppointmentRepository.class);var owner=s.getProfessional().getId();when(repo.findByIdAndProfessionalId(s.getId(),owner)).thenReturn(Optional.of(s));var service=new SessionAppointmentService(repo,mock(PatientRepository.class),mock(ProfessionalRepository.class),mock(ScheduleBlockRepository.class));assertSame(s,service.updateFinance(owner,s.getId(),BigDecimal.TEN,PaymentStatus.PENDING,null,null));assertThrows(SessionNotFoundException.class,()->service.updateFinance(UUID.randomUUID(),s.getId(),BigDecimal.TEN,PaymentStatus.PENDING,null,null));}
 @Test void paymentMethodIsClearedWhenNoLongerPaid(){var s=appointment();s.updateFinance(BigDecimal.TEN,PaymentStatus.PAID,LocalDate.now(),null);s.updatePaymentMethod(PaymentMethod.PIX);assertEquals(PaymentMethod.PIX,s.getPaymentMethod());s.updateFinance(BigDecimal.TEN,PaymentStatus.PENDING,null,null);assertNull(s.getPaymentMethod());s.updatePaymentMethod(PaymentMethod.CARD);assertNull(s.getPaymentMethod());}
 @Test void cancellationClearsPaymentMethod(){var s=appointment();s.updateFinance(BigDecimal.TEN,PaymentStatus.PAID,LocalDate.now(),null);s.updatePaymentMethod(PaymentMethod.CASH);s.cancelFromWhatsApp();assertNull(s.getPaymentMethod());}
}
