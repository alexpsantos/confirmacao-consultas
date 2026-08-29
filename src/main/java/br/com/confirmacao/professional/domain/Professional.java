package br.com.confirmacao.professional.domain;

import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "professionals")
public class Professional {

        @Id
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "tenant_id", nullable = false)
        private Tenant tenant;

        @Column(name = "full_name", nullable = false, length = 150)
        private String fullName;

        @Column(nullable = false, length = 254)
        private String email;

        @Column(length = 30)
        private String phone;

        @Column(name = "registration_number", length = 50)
        private String registrationNumber;

        @Column(nullable = false)
        private boolean active;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

    protected Professional() {
    }

    public Professional(
            Tenant tenant,
            String fullName,
            String email,
            String phone,
            String registrationNumber
    ) {
        if (tenant == null) {
            throw new IllegalArgumentException("O tenant é obrigatório");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("O nome do profissional é obrigatório");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail do profissional é obrigatório");
        }

        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.fullName = fullName.trim();
        this.email = email.trim().toLowerCase();
        this.phone = phone;
        this.registrationNumber = registrationNumber;
        this.active = true;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
