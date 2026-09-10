package br.com.confirmacao.audit.application;

import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.audit.domain.AuditLog;
import br.com.confirmacao.audit.infrastructure.AuditLogRepository;
import br.com.confirmacao.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuthenticated(AuditAction action, UUID resourceTenantId,
                                    String resourceType, UUID resourceId,
                                    HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID actorUserId = null;
        UUID actorTenantId = null;
        if (authentication instanceof JwtAuthenticationToken jwt) {
            actorUserId = parseUuid(jwt.getToken().getSubject());
            actorTenantId = parseUuid(jwt.getToken().getClaimAsString("tenant_id"));
        }
        save(actorUserId, actorTenantId, resourceTenantId, action,
                resourceType, resourceId, clientIp(request));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLogin(AuditAction action, UUID requestedTenantId,
                            User user, HttpServletRequest request) {
        save(user == null ? null : user.getId(),
                user == null ? null : user.getTenant().getId(), requestedTenantId,
                action, "AUTH", user == null ? null : user.getId(), clientIp(request));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPasswordReset(AuditAction action, User user,
                                    HttpServletRequest request) {
        save(user.getId(), user.getTenant().getId(), user.getTenant().getId(),
                action, "USER", user.getId(), clientIp(request));
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(UUID tenantId, Pageable pageable) {
        return repository.findAllByResourceTenantId(tenantId, pageable);
    }

    private void save(UUID actorUserId, UUID actorTenantId, UUID resourceTenantId,
                      AuditAction action, String resourceType, UUID resourceId,
                      String ipAddress) {
        repository.save(new AuditLog(actorUserId, actorTenantId, resourceTenantId,
                action, resourceType, resourceId, ipAddress));
    }

    private UUID parseUuid(String value) {
        try { return value == null ? null : UUID.fromString(value); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank()
                ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
