package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TenantSelectionTokenService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final String issuer;

    public TenantSelectionTokenService(JwtEncoder encoder, JwtDecoder decoder,
            @Value("${app.security.jwt.issuer}") String issuer) {
        this.encoder = encoder; this.decoder = decoder; this.issuer = issuer;
    }

    public String generate(String email, List<User> memberships) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now).expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(email).claim("purpose", "tenant_selection")
                .claim("tenant_ids", memberships.stream().map(u -> u.getTenant().getId().toString()).toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Selection validate(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            if (!"tenant_selection".equals(jwt.getClaimAsString("purpose"))) throw new InvalidCredentialsException();
            List<UUID> tenantIds = jwt.getClaimAsStringList("tenant_ids").stream().map(UUID::fromString).toList();
            return new Selection(jwt.getSubject(), tenantIds);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidCredentialsException();
        }
    }

    public record Selection(String email, List<UUID> tenantIds) {}
}
