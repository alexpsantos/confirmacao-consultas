package br.com.confirmacao.auth.application;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.security.SecurityConfig;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenServiceTest {

    private static final String ISSUER =
            "http://localhost:8080";

    private TokenService tokenService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        SecurityConfig securityConfig = new SecurityConfig();

        String encodedSecret = Base64.getEncoder().encodeToString(
                "01234567890123456789012345678901"
                        .getBytes(StandardCharsets.UTF_8)
        );

        SecretKey secretKey =
                securityConfig.jwtSecretKey(encodedSecret);
        JwtEncoder jwtEncoder =
                securityConfig.jwtEncoder(secretKey);

        jwtDecoder = securityConfig.jwtDecoder(
                secretKey,
                ISSUER
        );

        tokenService = new TokenService(
                jwtEncoder,
                ISSUER,
                Duration.ofHours(8)
        );
    }

    @Test
    void shouldGenerateOwnerToken() {
        Tenant tenant = createTenant();
        User user = new User(
                tenant,
                null,
                "Alex Santos",
                "alex@exemplo.com",
                "password-hash",
                UserRole.OWNER
        );

        Jwt jwt = jwtDecoder.decode(
                tokenService.generate(user)
        );

        assertEquals(ISSUER, jwt.getIssuer().toString());
        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals(
                tenant.getId().toString(),
                jwt.getClaimAsString("tenant_id")
        );
        assertEquals("Alex Santos", jwt.getClaimAsString("name"));
        assertEquals("OWNER", jwt.getClaimAsString("role"));
        assertNull(jwt.getClaim("professional_id"));
        assertEquals(
                Duration.ofHours(8),
                Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())
        );
        assertEquals(28_800L, tokenService.expirationSeconds());
    }

    @Test
    void shouldIncludeProfessionalIdInToken() {
        Tenant tenant = createTenant();
        Professional professional = new Professional(
                tenant,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );

        User user = new User(
                tenant,
                professional,
                "Ana Souza",
                "ana.login@exemplo.com",
                "password-hash",
                UserRole.PROFESSIONAL
        );

        Jwt jwt = jwtDecoder.decode(
                tokenService.generate(user)
        );

        assertEquals(
                professional.getId().toString(),
                jwt.getClaimAsString("professional_id")
        );
        assertEquals(
                "PROFESSIONAL",
                jwt.getClaimAsString("role")
        );
    }

    @Test
    void shouldGenerateGlobalAdminTokenWithoutTenant() {
        User admin = User.globalAdmin(
                "Administrador",
                "admin@confirmacao.com",
                "password-hash"
        );

        Jwt jwt = jwtDecoder.decode(tokenService.generate(admin));

        assertEquals("ADMIN", jwt.getClaimAsString("role"));
        assertNull(jwt.getClaim("tenant_id"));
        assertNull(admin.getTenant());
    }

    private Tenant createTenant() {
        return new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );
    }
}
