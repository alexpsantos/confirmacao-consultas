package br.com.confirmacao.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                authenticationEntryPoint
                        ).accessDeniedHandler(
                                accessDeniedHandler
                        )
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/api/v1/auth/**",
                                        "/api/v1/onboarding",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/actuator/health"
                                )
                                .permitAll()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/v1/tenants"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/tenants"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/tenants/*"
                                ).hasAnyRole("ADMIN", "OWNER")
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/v1/tenants/*"
                                ).hasAnyRole("ADMIN", "OWNER")
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/v1/tenants/*"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/v1/tenants/*/activate"
                                ).hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/tenants/*/professionals/**"
                                ).hasAnyRole(
                                        "ADMIN",
                                        "OWNER",
                                        "PROFESSIONAL"
                                )
                                .requestMatchers(
                                        "/api/v1/tenants/*/professionals/**"
                                ).hasAnyRole("ADMIN", "OWNER")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/v1/tenants/*/patients/**"
                                ).hasAnyRole("ADMIN", "OWNER", "PROFESSIONAL")
                                .requestMatchers(
                                        "/api/v1/tenants/*/patients/**"
                                ).hasAnyRole("ADMIN", "OWNER")
                                .requestMatchers(
                                        "/api/v1/tenants/*/users",
                                        "/api/v1/tenants/*/users/**"
                                ).hasAnyRole("ADMIN", "OWNER")
                                .anyRequest()
                                .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .jwt(jwt ->
                                        jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter
                                        )
                                )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.security.jwt.secret}") String encodedSecret
    ) {
        byte[] secretBytes =
                Base64.getDecoder().decode(encodedSecret);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET deve possuir pelo menos 256 bits"
            );
        }

        return new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            @Value("${app.security.jwt.issuer}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }
}
