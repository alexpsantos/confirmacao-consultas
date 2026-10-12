package br.com.confirmacao.session.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="session_reschedule_history")
public class SessionRescheduleHistory {
 @Id private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="session_id",nullable=false) private SessionAppointment session;
 @Column(name="previous_start",nullable=false) private Instant previousStart;
 @Column(name="previous_end",nullable=false) private Instant previousEnd;
 @Column(name="new_start",nullable=false) private Instant newStart;
 @Column(name="new_end",nullable=false) private Instant newEnd;
 @Column(length=500) private String reason;
 @Column(name="actor_name",nullable=false,length=150) private String actorName;
 @Column(name="changed_at",nullable=false) private Instant changedAt;
 protected SessionRescheduleHistory(){}
 public SessionRescheduleHistory(SessionAppointment session,Instant previousStart,Instant previousEnd,Instant newStart,Instant newEnd,String reason,String actorName){id=UUID.randomUUID();this.session=session;this.previousStart=previousStart;this.previousEnd=previousEnd;this.newStart=newStart;this.newEnd=newEnd;this.reason=reason;this.actorName=actorName;changedAt=Instant.now();}
 public UUID getId(){return id;}public Instant getPreviousStart(){return previousStart;}public Instant getPreviousEnd(){return previousEnd;}public Instant getNewStart(){return newStart;}public Instant getNewEnd(){return newEnd;}public String getReason(){return reason;}public String getActorName(){return actorName;}public Instant getChangedAt(){return changedAt;}
}
