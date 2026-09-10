package br.com.confirmacao.auth.domain;

import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {
    @Test void shouldBeValidBeforeExpirationAndOnlyOnce() {
        Instant now=Instant.now(); PasswordResetToken token=new PasswordResetToken(user(),"hash",now.plusSeconds(60));
        assertTrue(token.isValidAt(now)); token.markAsUsed(now); assertFalse(token.isValidAt(now));
    }
    @Test void shouldRejectExpiredToken() {
        Instant now=Instant.now(); PasswordResetToken token=new PasswordResetToken(user(),"hash",now.minusSeconds(1));
        assertFalse(token.isValidAt(now)); assertThrows(IllegalStateException.class,()->token.markAsUsed(now));
    }
    private User user(){ return new User(new Tenant("Clínica","America/Sao_Paulo"),null,
            "Alex","alex@email.com","hash",UserRole.OWNER); }
}
