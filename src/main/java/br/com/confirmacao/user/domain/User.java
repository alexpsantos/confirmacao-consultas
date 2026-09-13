package br.com.confirmacao.user.domain;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_tenant_email",
                        columnNames = {"tenant_id", "email"}
                ),
                @UniqueConstraint(
                        name = "uk_users_professional",
                        columnNames = {"professional_id"}
                )
        }
)
public class User {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id")
    private Professional professional;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(
            Tenant tenant,
            Professional professional,
            String name,
            String email,
            String passwordHash,
            UserRole role
    ) {
        if (role == null) {
            throw new IllegalArgumentException("O perfil do usuário é obrigatório");
        }

        if (role == UserRole.ADMIN && tenant != null) {
            throw new IllegalArgumentException("O administrador global não deve estar vinculado a um tenant");
        }

        if (role != UserRole.ADMIN && tenant == null) {
            throw new IllegalArgumentException("O tenant é obrigatório para usuários não administradores");
        }

        if (role == UserRole.PROFESSIONAL && professional == null) {
            throw new IllegalArgumentException(
                    "Um usuário profissional deve estar vinculado a um profissional"
            );
        }

        if (professional != null
                && !professional.getTenant().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException(
                    "O profissional deve pertencer ao mesmo tenant do usuário"
            );
        }

        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.professional = professional;
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.role = role;
        this.active = true;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static User globalAdmin(String name, String email, String passwordHash) {
        return new User(null, null, name, email, passwordHash, UserRole.ADMIN);
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do usuário é obrigatório"
            );
        }

        return name.trim();
    }

    private String validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail do usuário é obrigatório"
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "A senha criptografada é obrigatória"
            );
        }

        return passwordHash;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
        this.updatedAt = Instant.now();
    }

    public void update(String name, String email, UserRole role, Professional professional) {
        if (role == null) throw new IllegalArgumentException("O perfil do usuário é obrigatório");
        if (role == UserRole.ADMIN) throw new IllegalArgumentException("O perfil ADMIN é reservado à plataforma");
        if (role == UserRole.PROFESSIONAL && professional == null)
            throw new IllegalArgumentException("Um usuário profissional deve estar vinculado a um profissional");
        if (professional != null && !professional.getTenant().getId().equals(tenant.getId()))
            throw new IllegalArgumentException("O profissional deve pertencer ao mesmo tenant do usuário");
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.role = role;
        this.professional = professional;
        this.updatedAt = Instant.now();
    }

    public void linkProfessional(Professional professional) {
        if (professional == null) throw new IllegalArgumentException("O profissional é obrigatório");
        if (tenant == null || !professional.getTenant().getId().equals(tenant.getId()))
            throw new IllegalArgumentException("O profissional deve pertencer ao mesmo tenant do usuário");
        this.professional = professional;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Professional getProfessional() {
        return professional;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
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
