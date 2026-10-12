package br.com.confirmacao.session.application;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.session.domain.*;
import br.com.confirmacao.session.infrastructure.*;
import br.com.confirmacao.scheduleblock.infrastructure.ScheduleBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service public class SessionRescheduleService {
 public enum Scope { ONE, FOLLOWING }
 private final ProfessionalRepository professionals;private final SessionAppointmentRepository sessions;private final ScheduleBlockRepository blocks;private final SessionRescheduleHistoryRepository history;
 public SessionRescheduleService(ProfessionalRepository professionals,SessionAppointmentRepository sessions,ScheduleBlockRepository blocks,SessionRescheduleHistoryRepository history){this.professionals=professionals;this.sessions=sessions;this.blocks=blocks;this.history=history;}
 @Transactional public List<SessionAppointment> reschedule(UUID professionalId,UUID id,Instant start,Instant end,String reason,Scope scope){
  professionals.lockSchedule(professionalId);var anchor=find(professionalId,id);var now=Instant.now();eligible(anchor,now);
  if(start==null||end==null||!end.isAfter(start)||!start.isAfter(now))throw new IllegalArgumentException("Informe início futuro e término posterior ao início");
  if(reason!=null&&reason.length()>500)throw new IllegalArgumentException("O motivo deve ter até 500 caracteres");
  var duration=Duration.between(start,end);var zone=ZoneId.of(anchor.getProfessional().getTimezone());
  if(scope==Scope.FOLLOWING&&(anchor.getSeriesId()==null||anchor.getSeriesPosition()==null))throw new IllegalArgumentException("Esta sessão não possui uma série vinculada. Reagende apenas esta sessão");
  var targets=scope==Scope.ONE?List.of(anchor):sessions.findAllByProfessionalIdAndSeriesIdOrderBySeriesPositionAsc(professionalId,anchor.getSeriesId()).stream().filter(s->s.getSeriesPosition()>=anchor.getSeriesPosition()&&s.getStartsAt().isAfter(now)&&(s.getStatus()==SessionStatus.SCHEDULED||s.getStatus()==SessionStatus.CONFIRMED)).toList();
  record Move(SessionAppointment session,Instant start,Instant end){}
  var moves=new ArrayList<Move>();var ignored=targets.stream().map(SessionAppointment::getId).collect(java.util.stream.Collectors.toSet());
  for(var target:targets){eligible(target,now);var newStart=scope==Scope.ONE?start:start.atZone(zone).plusWeeks(target.getSeriesPosition()-anchor.getSeriesPosition()).toInstant();var newEnd=newStart.plus(duration);
   if(blocks.existsByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThan(professionalId,newEnd,newStart))throw new SessionConflictException("Existe um bloqueio em uma das novas datas. Nenhuma sessão foi reagendada");
   if(sessions.findAllByProfessionalIdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(professionalId,newEnd,newStart).stream().anyMatch(s->s.getStatus()!=SessionStatus.CANCELED&&!ignored.contains(s.getId())))throw new SessionConflictException("Existe uma sessão em uma das novas datas. Nenhuma sessão foi reagendada");
   if(moves.stream().anyMatch(m->newStart.isBefore(m.end())&&newEnd.isAfter(m.start())))throw new SessionConflictException("Os novos horários da série se sobrepõem");
   moves.add(new Move(target,newStart,newEnd));
  }
  for(var move:moves)move.session().reschedule(move.start(),move.end(),reason);
  return targets;
 }
 @Transactional public List<SessionAppointment> cancel(UUID professionalId,UUID id,String reason,Scope scope){
  professionals.lockSchedule(professionalId);var anchor=find(professionalId,id);var now=Instant.now();
  if(!anchor.getStartsAt().isAfter(now)||(anchor.getStatus()!=SessionStatus.SCHEDULED&&anchor.getStatus()!=SessionStatus.CONFIRMED))throw new IllegalArgumentException("Apenas sessões futuras agendadas ou confirmadas podem ser canceladas");
  if(reason!=null&&reason.length()>500)throw new IllegalArgumentException("O motivo deve ter até 500 caracteres");
  if(scope==Scope.FOLLOWING&&anchor.getSeriesId()==null)throw new IllegalArgumentException("Esta sessão não possui uma série vinculada");
  var targets=scope==Scope.ONE?List.of(anchor):sessions.findAllByProfessionalIdAndSeriesIdOrderBySeriesPositionAsc(professionalId,anchor.getSeriesId()).stream().filter(s->s.getSeriesPosition()>=anchor.getSeriesPosition()&&s.getStartsAt().isAfter(now)&&(s.getStatus()==SessionStatus.SCHEDULED||s.getStatus()==SessionStatus.CONFIRMED)).toList();
  targets.forEach(s->s.cancelScheduled(reason));return targets;
 }
 private void eligible(SessionAppointment s,Instant now){if(!s.getStartsAt().isAfter(now)||(s.getStatus()!=SessionStatus.SCHEDULED&&s.getStatus()!=SessionStatus.CONFIRMED))throw new IllegalArgumentException("Apenas sessões futuras agendadas ou confirmadas podem ser reagendadas");if(!s.getPatient().isActive())throw new IllegalArgumentException("Ative o paciente antes de reagendar");}
 private SessionAppointment find(UUID professionalId,UUID id){return sessions.findByIdAndProfessionalId(id,professionalId).orElseThrow(()->new SessionNotFoundException(id));}
 @Transactional(readOnly=true) public List<SessionRescheduleHistory> history(UUID professionalId,UUID id){find(professionalId,id);return history.findAllBySessionIdAndSessionProfessionalIdOrderByChangedAtDescIdDesc(id,professionalId);}
}
