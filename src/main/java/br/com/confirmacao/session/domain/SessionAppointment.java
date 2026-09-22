package br.com.confirmacao.session.domain;

import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.professional.domain.Professional;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="sessions")
public class SessionAppointment {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="professional_id",nullable=false) private Professional professional;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="patient_id",nullable=false) private Patient patient;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private SessionModality modality;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private SessionStatus status;
    @Column(name="meeting_link",length=500) private String meetingLink;
    @Column(length=500) private String notes;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected SessionAppointment(){}
    public SessionAppointment(Professional p,Patient patient,Instant start,Instant end,SessionModality modality,String link,String notes){id=UUID.randomUUID();professional=p;this.patient=patient;apply(start,end,modality,SessionStatus.SCHEDULED,link,notes);createdAt=updatedAt=Instant.now();}
    public void update(Patient patient,Instant start,Instant end,SessionModality modality,SessionStatus status,String link,String notes){this.patient=patient;apply(start,end,modality,status,link,notes);updatedAt=Instant.now();}
    public void updateResult(SessionStatus status,String notes){if(status!=SessionStatus.COMPLETED&&status!=SessionStatus.CANCELED&&status!=SessionStatus.NO_SHOW)throw new IllegalArgumentException("Informe o resultado final da sessão");this.status=status;this.notes=clean(notes);updatedAt=Instant.now();}
    private void apply(Instant start,Instant end,SessionModality modality,SessionStatus status,String link,String notes){if(start==null||end==null||!end.isAfter(start))throw new IllegalArgumentException("O horário final deve ser posterior ao inicial");if(modality==null)throw new IllegalArgumentException("A modalidade é obrigatória");if(status==null)throw new IllegalArgumentException("O status é obrigatório");startsAt=start;endsAt=end;this.modality=modality;this.status=status;meetingLink=clean(link);this.notes=clean(notes);}
    private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    public UUID getId(){return id;} public Professional getProfessional(){return professional;} public Patient getPatient(){return patient;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public SessionModality getModality(){return modality;} public SessionStatus getStatus(){return status;} public String getMeetingLink(){return meetingLink;} public String getNotes(){return notes;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
