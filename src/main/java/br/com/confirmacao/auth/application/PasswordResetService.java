package br.com.confirmacao.auth.application;

import br.com.confirmacao.auth.domain.PasswordResetToken;
import br.com.confirmacao.auth.infrastructure.PasswordResetTokenRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class PasswordResetService {
    private final UserRepository users; private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder; private final SecureRandom secureRandom = new SecureRandom();
    private final Duration expiration; private final boolean exposeToken;
    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
            PasswordEncoder passwordEncoder,
            @Value("${app.password-reset.expiration:PT30M}") Duration expiration,
            @Value("${app.password-reset.expose-token:false}") boolean exposeToken) {
        this.users=users; this.tokens=tokens; this.passwordEncoder=passwordEncoder;
        this.expiration=expiration; this.exposeToken=exposeToken;
    }
    @Transactional
    public PasswordResetRequestResult request(UUID tenantId, String email) {
        Optional<User> found=users.findByTenantIdAndEmail(tenantId,email.trim().toLowerCase(Locale.ROOT));
        if (found.isEmpty() || !found.get().isActive() || !found.get().getTenant().isActive())
            return new PasswordResetRequestResult(null,null);
        User user=found.get(); Instant now=Instant.now();
        tokens.invalidateUnusedByUserId(user.getId(),now);
        String raw=generateToken();
        tokens.save(new PasswordResetToken(user,hash(raw),now.plus(expiration)));
        return new PasswordResetRequestResult(exposeToken?raw:null,user);
    }
    @Transactional
    public User reset(String rawToken,String newPassword) {
        Instant now=Instant.now();
        PasswordResetToken token=tokens.findByTokenHash(hash(rawToken))
                .filter(value->value.isValidAt(now)).orElseThrow(InvalidPasswordResetTokenException::new);
        User user=token.getUser();
        if(!user.isActive() || !user.getTenant().isActive()) throw new InvalidPasswordResetTokenException();
        user.changePasswordHash(passwordEncoder.encode(newPassword)); token.markAsUsed(now); return user;
    }
    private String generateToken(){ byte[] bytes=new byte[32]; secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String hash(String value){
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch(NoSuchAlgorithmException e){ throw new IllegalStateException("Não foi possível processar o token",e); }
    }
}
