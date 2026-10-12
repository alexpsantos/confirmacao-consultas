package br.com.confirmacao.session.domain;

import br.com.confirmacao.patient.domain.Patient;
import br.com.confirmacao.professional.domain.Professional;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
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
    @Column(name="financial_amount",nullable=false,precision=10,scale=2) private BigDecimal financialAmount;
    @Enumerated(EnumType.STRING) @Column(name="payment_status",nullable=false,length=20) private PaymentStatus paymentStatus=PaymentStatus.PENDING;
    @Column(name="payment_date") private LocalDate paymentDate;
    @Column(name="financial_notes",length=500) private String financialNotes;
    @Column(name="financial_amount_overridden",nullable=false) private boolean financialAmountOverridden;
    @Enumerated(EnumType.STRING) @Column(name="payment_method",length=20) private PaymentMethod paymentMethod;
    public PaymentMethod getPaymentMethod(){return paymentMethod;}
    public void updatePaymentMethod(PaymentMethod method){paymentMethod=paymentStatus==PaymentStatus.PAID?method:null;}
    public BigDecimal getFinancialAmount(){return financialAmount;}
    public PaymentStatus getPaymentStatus(){return paymentStatus;}
    public LocalDate getPaymentDate(){return paymentDate;}
    public String getFinancialNotes(){return financialNotes;}
    public void updateFinance(BigDecimal amount,PaymentStatus paymentStatus,LocalDate date,String notes){
        if(amount==null||amount.signum()<0||amount.scale()>2||amount.compareTo(new BigDecimal("99999999.99"))>0)throw new IllegalArgumentException("Informe um valor válido com até duas casas decimais");
        if(paymentStatus==null)throw new IllegalArgumentException("A situação financeira é obrigatória");
        if(status==SessionStatus.CANCELED&&paymentStatus!=PaymentStatus.CANCELED&&paymentStatus!=PaymentStatus.EXEMPT)throw new IllegalArgumentException("Sessões canceladas devem permanecer canceladas ou isentas no financeiro");
        if(paymentStatus==PaymentStatus.PAID&&date==null)throw new IllegalArgumentException("Informe a data de pagamento");
        if(date!=null&&paymentStatus!=PaymentStatus.PAID)throw new IllegalArgumentException("Data de pagamento permitida apenas para lançamentos pagos");
        if(notes!=null&&notes.length()>500)throw new IllegalArgumentException("A observação deve ter até 500 caracteres");
        if(financialAmount.compareTo(amount)!=0)financialAmountOverridden=true;
        financialAmount=amount;this.paymentStatus=paymentStatus;paymentDate=date;if(paymentStatus!=PaymentStatus.PAID)paymentMethod=null;financialNotes=clean(notes);updatedAt=Instant.now();
    }
    private void cancelFinance(){if(paymentStatus!=PaymentStatus.EXEMPT)paymentStatus=PaymentStatus.CANCELED;paymentDate=null;paymentMethod=null;}
    @Column(name="series_id") private UUID seriesId;
    @Column(name="series_position") private Integer seriesPosition;
    @OneToMany(mappedBy="session",cascade=CascadeType.PERSIST) private java.util.List<SessionRescheduleHistory> rescheduleHistory=new java.util.ArrayList<>();
    public UUID getSeriesId(){return seriesId;}
    public Integer getSeriesPosition(){return seriesPosition;}
    public void assignSeries(UUID id,int position){seriesId=id;seriesPosition=position;}
    public void reschedule(Instant start,Instant end,String reason){
        if(start==null||end==null||!end.isAfter(start))throw new IllegalArgumentException("O horário final deve ser posterior ao inicial");
        if(reason!=null&&reason.length()>500)throw new IllegalArgumentException("O motivo deve ter até 500 caracteres");
        recordReschedule(start,end,reason);startsAt=start;endsAt=end;updatedAt=Instant.now();
    }
    private void recordReschedule(Instant start,Instant end,String reason){if(startsAt!=null&&(!startsAt.equals(start)||!endsAt.equals(end)))rescheduleHistory.add(new SessionRescheduleHistory(this,startsAt,endsAt,start,end,clean(reason),professional.getFullName()));}
    @Column(name="cancellation_reason",length=500) private String cancellationReason;
    public String getCancellationReason(){return cancellationReason;}
    public void cancelScheduled(String reason){if(status!=SessionStatus.SCHEDULED&&status!=SessionStatus.CONFIRMED)throw new IllegalArgumentException("Apenas sessões agendadas ou confirmadas podem ser canceladas");if(reason!=null&&reason.length()>500)throw new IllegalArgumentException("O motivo deve ter até 500 caracteres");cancellationReason=clean(reason);status=SessionStatus.CANCELED;cancelFinance();updatedAt=Instant.now();}
    protected SessionAppointment(){}
    public SessionAppointment(Professional p,Patient patient,Instant start,Instant end,SessionModality modality,String link,String notes){id=UUID.randomUUID();professional=p;financialAmount=p.getDefaultSessionPrice();this.patient=patient;apply(start,end,modality,SessionStatus.SCHEDULED,link,notes);createdAt=updatedAt=Instant.now();}
    public void update(Patient patient,Instant start,Instant end,SessionModality modality,SessionStatus status,String link,String notes){recordReschedule(start,end,null);this.patient=patient;apply(start,end,modality,status,link,notes);updatedAt=Instant.now();}
    public void updateResult(SessionStatus status,String notes){if(status!=SessionStatus.COMPLETED&&status!=SessionStatus.NO_SHOW)throw new IllegalArgumentException("Informe realizada ou não compareceu");this.status=status;this.notes=clean(notes);updatedAt=Instant.now();}
    public boolean confirmFromWhatsApp(){if(status!=SessionStatus.SCHEDULED)throw new IllegalArgumentException("A sessão não pode mais ser confirmada");status=SessionStatus.CONFIRMED;updatedAt=Instant.now();return true;}
    public boolean cancelFromWhatsApp(){if(status!=SessionStatus.SCHEDULED&&status!=SessionStatus.CONFIRMED)throw new IllegalArgumentException("A sessão não pode mais ser cancelada");status=SessionStatus.CANCELED;cancelFinance();updatedAt=Instant.now();return true;}
    private void apply(Instant start,Instant end,SessionModality modality,SessionStatus status,String link,String notes){if(start==null||end==null||!end.isAfter(start))throw new IllegalArgumentException("O horário final deve ser posterior ao inicial");if(modality==null)throw new IllegalArgumentException("A modalidade é obrigatória");if(status==null)throw new IllegalArgumentException("O status é obrigatório");startsAt=start;endsAt=end;this.modality=modality;this.status=status;if(status==SessionStatus.CANCELED)cancelFinance();meetingLink=clean(link);this.notes=clean(notes);}
    private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
    public UUID getId(){return id;} public Professional getProfessional(){return professional;} public Patient getPatient(){return patient;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public SessionModality getModality(){return modality;} public SessionStatus getStatus(){return status;} public String getMeetingLink(){return meetingLink;} public String getNotes(){return notes;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
