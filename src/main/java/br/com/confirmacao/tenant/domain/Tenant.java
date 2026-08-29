package br.com.confirmacao.tenant.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(nullable = false, length = 60)
    private String timezone;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    protected Tenant() {

    }

    public Tenant(String displayName, String timezone) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do tenant é obrigatório"
            );
        }

        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException(
                    "O timezone é obrigatório"
            );
        }

        this.id = UUID.randomUUID();
        this.displayName = displayName;
        this.timezone = timezone;
        this.active = true;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTimezone() {
        return timezone;
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

    public void update(String displayName, String timezone) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("O nome do tenant é obrigatório");
        }

        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("O timezone é obrigatório");
        }

        this.displayName = displayName;
        this.timezone = timezone;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }
}