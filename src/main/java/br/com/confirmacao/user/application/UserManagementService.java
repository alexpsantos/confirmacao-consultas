package br.com.confirmacao.user.application;

import br.com.confirmacao.professional.application.ProfessionalNotFoundException;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.professional.infrastructure.ProfessionalRepository;
import br.com.confirmacao.tenant.application.TenantInactiveException;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import br.com.confirmacao.user.infrastructure.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserManagementService {
    private final UserRepository users;
    private final TenantRepository tenants;
    private final ProfessionalRepository professionals;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository users, TenantRepository tenants,
            ProfessionalRepository professionals, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.tenants = tenants;
        this.professionals = professionals;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User create(UUID tenantId, String name, String email, String password,
                       UserRole role, UUID professionalId) {
        Tenant tenant = activeTenant(tenantId);
        validateManagedRole(role);
        String normalizedEmail = normalizeEmail(email);
        if (users.existsByTenantIdAndEmail(tenantId, normalizedEmail))
            throw new UserAlreadyExistsException("Já existe um usuário com este e-mail nesta clínica");
        Professional professional = resolveProfessional(tenantId, role, professionalId);
        if (professional != null && users.findByProfessionalId(professional.getId()).isPresent())
            throw new UserAlreadyExistsException("Este profissional já possui um usuário");
        return users.save(new User(tenant, professional, name, normalizedEmail,
                passwordEncoder.encode(password), role));
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(UUID tenantId, Pageable pageable) {
        activeTenant(tenantId);
        return users.findAllByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public User findById(UUID tenantId, UUID userId) { return managedUser(tenantId, userId); }

    @Transactional
    public User update(UUID tenantId, UUID userId, String name, String email,
                       UserRole role, UUID professionalId) {
        User user = managedUser(tenantId, userId);
        validateManagedRole(role);
        String normalizedEmail = normalizeEmail(email);
        if (users.existsByTenantIdAndEmailAndIdNot(tenantId, normalizedEmail, userId))
            throw new UserAlreadyExistsException("Já existe um usuário com este e-mail nesta clínica");
        Professional professional = resolveProfessional(tenantId, role, professionalId);
        if (professionalId != null) {
            users.findByProfessionalId(professionalId)
                    .filter(existing -> !existing.getId().equals(userId))
                    .ifPresent(existing -> { throw new UserAlreadyExistsException("Este profissional já possui um usuário"); });
        }
        user.update(name, normalizedEmail, role, professional);
        return user;
    }

    @Transactional public void deactivate(UUID tenantId, UUID userId) { managedUser(tenantId, userId).deactivate(); }
    @Transactional public void activate(UUID tenantId, UUID userId) { managedUser(tenantId, userId).activate(); }
    @Transactional public void changePassword(UUID tenantId, UUID userId, String password) {
        managedUser(tenantId, userId).changePasswordHash(passwordEncoder.encode(password));
    }

    private User managedUser(UUID tenantId, UUID userId) {
        activeTenant(tenantId);
        return users.findByIdAndTenantId(userId, tenantId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Professional resolveProfessional(UUID tenantId, UserRole role, UUID professionalId) {
        if (role == UserRole.PROFESSIONAL) {
            if (professionalId == null) throw new IllegalArgumentException("O professionalId é obrigatório para este perfil");
            return professionals.findByIdAndTenant_Id(professionalId, tenantId)
                    .orElseThrow(() -> new ProfessionalNotFoundException(professionalId));
        }
        if (professionalId != null) throw new IllegalArgumentException("Somente o perfil PROFESSIONAL pode ter professionalId");
        return null;
    }

    private void validateManagedRole(UserRole role) {
        if (role == UserRole.ADMIN) throw new IllegalArgumentException("O perfil ADMIN é reservado à plataforma");
    }

    private Tenant activeTenant(UUID tenantId) {
        Tenant tenant = tenants.findById(tenantId).orElseThrow(() -> new TenantNotFoundException(tenantId));
        if (!tenant.isActive()) throw new TenantInactiveException(tenantId);
        return tenant;
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
