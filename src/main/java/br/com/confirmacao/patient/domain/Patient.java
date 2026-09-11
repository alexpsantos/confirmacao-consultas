package br.com.confirmacao.patient.domain;

import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;
    @Column(name = "birth_date") private LocalDate birthDate;
    @Column(nullable = false, length = 20) private String phone;
    @Column(length = 254) private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_channel", nullable = false, length = 20)
    private PreferredContactChannel preferredChannel;
    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status", nullable = false, length = 20)
    private ConsentStatus consentStatus;
    @Column(name = "consented_at") private Instant consentedAt;
    @Column(nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Patient() {}

    public Patient(Tenant tenant, String fullName, LocalDate birthDate, String phone,
                   String email, PreferredContactChannel preferredChannel) {
        if (tenant == null) throw new IllegalArgumentException("O tenant é obrigatório");
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.fullName = validateFullName(fullName);
        this.birthDate = validateBirthDate(birthDate);
        this.phone = validatePhone(phone);
        this.email = normalizeEmail(email);
        this.preferredChannel = validatePreferredChannel(preferredChannel);
        this.consentStatus = ConsentStatus.PENDING;
        this.active = true;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String fullName, LocalDate birthDate, String phone, String email,
                       PreferredContactChannel preferredChannel) {
        this.fullName = validateFullName(fullName);
        this.birthDate = validateBirthDate(birthDate);
        this.phone = validatePhone(phone);
        this.email = normalizeEmail(email);
        this.preferredChannel = validatePreferredChannel(preferredChannel);
        this.updatedAt = Instant.now();
    }

    public void grantConsent() {
        consentStatus = ConsentStatus.GRANTED;
        consentedAt = Instant.now();
        updatedAt = consentedAt;
    }

    public void revokeConsent() {
        consentStatus = ConsentStatus.REVOKED;
        consentedAt = null;
        updatedAt = Instant.now();
    }

    public void deactivate() { active = false; updatedAt = Instant.now(); }
    public void activate() { active = true; updatedAt = Instant.now(); }

    private String validateFullName(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("O nome do paciente é obrigatório");
        return value.trim();
    }

    private LocalDate validateBirthDate(LocalDate value) {
        if (value != null && value.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("A data de nascimento não pode estar no futuro");
        return value;
    }

    private String validatePhone(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("O telefone do paciente é obrigatório");
        String normalized = value.replaceAll("\\D", "");
        if (normalized.length() < 10 || normalized.length() > 15)
            throw new IllegalArgumentException("O telefone do paciente é inválido");
        return normalized;
    }

    private String normalizeEmail(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private PreferredContactChannel validatePreferredChannel(PreferredContactChannel value) {
        if (value == null) throw new IllegalArgumentException("O canal preferencial é obrigatório");
        return value;
    }

    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public String getFullName() { return fullName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public PreferredContactChannel getPreferredChannel() { return preferredChannel; }
    public ConsentStatus getConsentStatus() { return consentStatus; }
    public Instant getConsentedAt() { return consentedAt; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
