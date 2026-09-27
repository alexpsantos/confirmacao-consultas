package br.com.confirmacao.whatsapp.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="whatsapp_messages")
public class WhatsAppMessage {
    @Id private UUID id;
    @Column(name="session_id", nullable=false) private UUID sessionId;
    @Column(name="patient_id", nullable=false) private UUID patientId;
    @Column(name="professional_id", nullable=false) private UUID professionalId;
    @Column(name="meta_message_id", length=120, unique=true) private String metaMessageId;
    @Column(name="idempotency_key", nullable=false, length=160, unique=true) private String idempotencyKey;
    @Column(name="template_name", nullable=false, length=100) private String templateName;
    @Enumerated(EnumType.STRING) @Column(name="delivery_status", nullable=false, length=20) private WhatsAppDeliveryStatus deliveryStatus;
    @Enumerated(EnumType.STRING) @Column(name="confirmation_status", nullable=false, length=20) private WhatsAppConfirmationStatus confirmationStatus;
    @Column(name="attempt_count", nullable=false) private int attemptCount;
    @Column(name="last_attempt_at") private Instant lastAttemptAt;
    @Column(name="last_error", length=500) private String lastError;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    protected WhatsAppMessage() {}
    public WhatsAppMessage(UUID sessionId, UUID patientId, UUID professionalId, String key, String template) {
        id=UUID.randomUUID(); this.sessionId=sessionId; this.patientId=patientId; this.professionalId=professionalId; idempotencyKey=key; templateName=template;
        deliveryStatus=WhatsAppDeliveryStatus.QUEUED; confirmationStatus=WhatsAppConfirmationStatus.PENDING; createdAt=updatedAt=Instant.now();
    }
    public void sent(String providerId) { metaMessageId=providerId; deliveryStatus=WhatsAppDeliveryStatus.SENT; attemptCount++; lastAttemptAt=updatedAt=Instant.now(); lastError=null; }
    public void failed(String error) { deliveryStatus=WhatsAppDeliveryStatus.FAILED; attemptCount++; lastAttemptAt=updatedAt=Instant.now(); lastError=error==null?null:error.substring(0, Math.min(error.length(), 500)); }
    public boolean updateDelivery(WhatsAppDeliveryStatus status) {
        if (status == null || deliveryStatus == status || deliveryStatus == WhatsAppDeliveryStatus.FAILED) return false;
        if (status != WhatsAppDeliveryStatus.FAILED && deliveryRank(status) <= deliveryRank(deliveryStatus)) return false;
        deliveryStatus=status; updatedAt=Instant.now(); return true;
    }
    private int deliveryRank(WhatsAppDeliveryStatus status) { return switch (status) { case QUEUED -> 0; case SENT -> 1; case DELIVERED -> 2; case READ -> 3; case FAILED -> 4; }; }
    public boolean confirm(){if(confirmationStatus==WhatsAppConfirmationStatus.CONFIRMED)return false;if(confirmationStatus!=WhatsAppConfirmationStatus.PENDING)throw new IllegalArgumentException("A confirmação da mensagem já foi cancelada");confirmationStatus=WhatsAppConfirmationStatus.CONFIRMED;updatedAt=Instant.now();return true;}
    public boolean cancel(){if(confirmationStatus==WhatsAppConfirmationStatus.CANCELED)return false;if(confirmationStatus!=WhatsAppConfirmationStatus.PENDING)throw new IllegalArgumentException("A confirmação da mensagem já foi confirmada");confirmationStatus=WhatsAppConfirmationStatus.CANCELED;updatedAt=Instant.now();return true;}
    public UUID getId(){return id;} public UUID getSessionId(){return sessionId;} public UUID getPatientId(){return patientId;} public UUID getProfessionalId(){return professionalId;} public String getMetaMessageId(){return metaMessageId;} public String getIdempotencyKey(){return idempotencyKey;} public String getTemplateName(){return templateName;} public WhatsAppDeliveryStatus getDeliveryStatus(){return deliveryStatus;} public WhatsAppConfirmationStatus getConfirmationStatus(){return confirmationStatus;} public int getAttemptCount(){return attemptCount;} public Instant getLastAttemptAt(){return lastAttemptAt;}
}
