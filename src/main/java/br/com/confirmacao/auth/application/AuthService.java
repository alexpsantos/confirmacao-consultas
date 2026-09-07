package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(
            UUID tenantId,
            String email,
            String password
    ) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByTenantIdAndEmail(tenantId, normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()
                || !user.getTenant().isActive()
                || !passwordEncoder.matches(
                        password,
                        user.getPasswordHash()
                )) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenService.generate(user);

        return new AuthenticationResult(
                accessToken,
                tokenService.expirationSeconds(),
                user
        );
    }
}
