package br.com.confirmacao.auth.domain;

import br.com.confirmacao.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected PasswordResetToken() {}
    public PasswordResetToken(User user, String tokenHash, Instant expiresAt) {
        if (user == null || tokenHash == null || tokenHash.isBlank() || expiresAt == null)
            throw new IllegalArgumentException("Usuário, token e expiração são obrigatórios");
        this.id = UUID.randomUUID(); this.user = user; this.tokenHash = tokenHash;
        this.expiresAt = expiresAt; this.createdAt = Instant.now();
    }
    public boolean isValidAt(Instant instant) { return usedAt == null && expiresAt.isAfter(instant); }
    public void markAsUsed(Instant instant) {
        if (!isValidAt(instant)) throw new IllegalStateException("Token inválido ou expirado");
        usedAt = instant;
    }
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
