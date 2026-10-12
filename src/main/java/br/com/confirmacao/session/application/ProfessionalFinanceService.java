package br.com.confirmacao.session.application;

import br.com.confirmacao.professional.application.ProfessionalNotFoundException;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.api.SessionResponse;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.infrastructure.SessionAppointmentRepository;
import br.com.confirmacao.shared.api.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ProfessionalFinanceService {
 public enum DateBasis { SESSION, PAYMENT }
 public record Totals(BigDecimal received,BigDecimal pending,BigDecimal exempt,BigDecimal total){}
 public record Result(PageResponse<SessionResponse> entries,Totals totals){}
 private final SessionAppointmentRepository sessions;
 private final ProfessionalRepository professionals;
 private final EntityManager em;
 public ProfessionalFinanceService(SessionAppointmentRepository sessions,ProfessionalRepository professionals,EntityManager em){this.sessions=sessions;this.professionals=professionals;this.em=em;}
 @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
 public Result find(UUID professionalId,UUID patientId,LocalDate from,LocalDate to,DateBasis basis,PaymentStatus status,PaymentMethod method,boolean unspecifiedMethod,int page,int size){
  if(from!=null&&to!=null&&to.isBefore(from))throw new IllegalArgumentException("A data final deve ser igual ou posterior à inicial");
  if(page<0||size<1||size>100)throw new IllegalArgumentException("Paginação inválida");
  if(method!=null&&unspecifiedMethod)throw new IllegalArgumentException("Escolha uma forma de pagamento ou não informada");
  var professional=professionals.findById(professionalId).orElseThrow(()->new ProfessionalNotFoundException(professionalId));
  var zone=ZoneId.of(professional.getTimezone());
  Specification<SessionAppointment> filter=(root,query,cb)->conditions(root,cb,professionalId,patientId,from,to,basis,status,method,unspecifiedMethod,zone);
  var entries=sessions.findAll(filter,PageRequest.of(page,size,Sort.by(Sort.Order.desc("startsAt"),Sort.Order.desc("id"))));
  var cb=em.getCriteriaBuilder();var query=cb.createTupleQuery();var root=query.from(SessionAppointment.class);
  query.multiselect(sum(cb,root,PaymentStatus.PAID).alias("received"),sum(cb,root,PaymentStatus.PENDING).alias("pending"),sum(cb,root,PaymentStatus.EXEMPT).alias("exempt"));
  query.where(conditions(root,cb,professionalId,patientId,from,to,basis,status,method,unspecifiedMethod,zone));
  var sums=em.createQuery(query).getSingleResult();var received=sums.get("received",BigDecimal.class);var pending=sums.get("pending",BigDecimal.class);var exempt=sums.get("exempt",BigDecimal.class);
  return new Result(PageResponse.from(entries.map(SessionResponse::from)),new Totals(received,pending,exempt,received.add(pending)));
 }
 private Expression<BigDecimal> sum(CriteriaBuilder cb,Root<SessionAppointment> root,PaymentStatus status){
  var eligible=cb.and(cb.notEqual(root.get("status"),SessionStatus.CANCELED),cb.equal(root.get("paymentStatus"),status));
  var amount=cb.<BigDecimal>selectCase().when(eligible,root.<BigDecimal>get("financialAmount")).otherwise(BigDecimal.ZERO);
  return cb.coalesce(cb.sum(amount),BigDecimal.ZERO);
 }
 private Predicate conditions(Root<SessionAppointment> root,CriteriaBuilder cb,UUID professionalId,UUID patientId,LocalDate from,LocalDate to,DateBasis basis,PaymentStatus status,PaymentMethod method,boolean unspecifiedMethod,ZoneId zone){
  var predicates=new ArrayList<Predicate>();predicates.add(cb.equal(root.get("professional").get("id"),professionalId));
  if(patientId!=null)predicates.add(cb.equal(root.get("patient").get("id"),patientId));
  if(status!=null)predicates.add(cb.equal(root.get("paymentStatus"),status));
  if(method!=null)predicates.add(cb.equal(root.get("paymentMethod"),method));
  if(unspecifiedMethod)predicates.add(cb.isNull(root.get("paymentMethod")));
  if(basis==DateBasis.PAYMENT){if(from!=null)predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"),from));if(to!=null)predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"),to));}
  else {if(from!=null)predicates.add(cb.greaterThanOrEqualTo(root.get("startsAt"),from.atStartOfDay(zone).toInstant()));if(to!=null)predicates.add(cb.lessThan(root.get("startsAt"),to.plusDays(1).atStartOfDay(zone).toInstant()));}
  return cb.and(predicates.toArray(Predicate[]::new));
 }
}
