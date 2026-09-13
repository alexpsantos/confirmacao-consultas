package br.com.confirmacao.auth.application;

import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TenantSelectionTokenService selectionTokenService;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            TenantSelectionTokenService selectionTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.selectionTokenService = selectionTokenService;
    }

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this(userRepository, passwordEncoder, tokenService, null);
    }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(
            String email,
            String password
    ) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        List<User> memberships = userRepository.findAllByEmailIgnoreCase(normalizedEmail).stream()
                .filter(User::isActive)
                .filter(user -> user.getRole() == UserRole.ADMIN
                        || user.getTenant() != null && user.getTenant().isActive())
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash())).toList();
        if (memberships.isEmpty()) throw new InvalidCredentialsException();
        var globalAdmin = memberships.stream()
                .filter(user -> user.getRole() == UserRole.ADMIN)
                .findFirst();
        if (globalAdmin.isPresent()) return authenticated(globalAdmin.get());
        if (memberships.size() == 1) return authenticated(memberships.getFirst());
        return AuthenticationResult.selectionRequired(
                selectionTokenService.generate(normalizedEmail, memberships), memberships);
    }

    @Deprecated
    public AuthenticationResult authenticate(UUID tenantId, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByTenantIdAndEmail(tenantId, normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive() || user.getTenant() == null || !user.getTenant().isActive()
                || !passwordEncoder.matches(password, user.getPasswordHash()))
            throw new InvalidCredentialsException();
        return authenticated(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResult selectTenant(String selectionToken, UUID tenantId) {
        var selection = selectionTokenService.validate(selectionToken);
        if (!selection.tenantIds().contains(tenantId)) throw new InvalidCredentialsException();
        User membership = userRepository.findByTenantIdAndEmail(tenantId, selection.email())
                .filter(User::isActive).filter(user -> user.getTenant().isActive())
                .orElseThrow(InvalidCredentialsException::new);
        return authenticated(membership);
    }

    private AuthenticationResult authenticated(User user) {
        return AuthenticationResult.authenticated(tokenService.generate(user), tokenService.expirationSeconds(), user);
    }
}
