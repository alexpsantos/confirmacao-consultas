package br.com.confirmacao.auth.application;

import br.com.confirmacao.auth.domain.PasswordResetToken;
import br.com.confirmacao.auth.infrastructure.PasswordResetTokenRepository;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.*;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock UserRepository users; @Mock PasswordResetTokenRepository tokens; @Mock PasswordEncoder encoder;
    PasswordResetService service; Tenant tenant; User user;
    @BeforeEach void setUp(){ service=new PasswordResetService(users,tokens,encoder,Duration.ofMinutes(30),true);
        tenant=new Tenant("Clínica","America/Sao_Paulo");
        user=new User(tenant,null,"Alex","alex@email.com","old-hash",UserRole.OWNER); }
    @Test void shouldCreateOpaqueTokenForActiveUser(){
        when(users.findByTenantIdAndEmail(tenant.getId(),"alex@email.com")).thenReturn(Optional.of(user));
        PasswordResetRequestResult result=service.request(tenant.getId(),"  ALEX@EMAIL.COM ");
        assertNotNull(result.token()); assertEquals(user,result.user());
        verify(tokens).invalidateUnusedByUserId(eq(user.getId()),any(Instant.class));
        verify(tokens).save(any(PasswordResetToken.class));
    }
    @Test void shouldNotRevealUnknownEmail(){
        when(users.findByTenantIdAndEmail(tenant.getId(),"unknown@email.com")).thenReturn(Optional.empty());
        PasswordResetRequestResult result=service.request(tenant.getId(),"unknown@email.com");
        assertNull(result.token()); assertNull(result.user()); verifyNoInteractions(tokens);
    }
    @Test void shouldResetPasswordAndConsumeToken(){
        PasswordResetToken token=new PasswordResetToken(user,"hash",Instant.now().plusSeconds(60));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(encoder.encode("NovaSenha1")).thenReturn("new-hash");
        assertSame(user,service.reset("raw-token","NovaSenha1"));
        assertEquals("new-hash",user.getPasswordHash()); assertNotNull(token.getUsedAt());
    }
    @Test void shouldRejectMissingOrExpiredToken(){
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidPasswordResetTokenException.class,()->service.reset("invalid","NovaSenha1"));
        verifyNoInteractions(encoder);
    }
}
