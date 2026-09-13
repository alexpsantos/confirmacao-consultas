package br.com.confirmacao.user.api;

import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.domain.AuditAction;
import br.com.confirmacao.shared.api.PageResponse;
import br.com.confirmacao.user.application.UserManagementService;
import br.com.confirmacao.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/users")
@PreAuthorize("@tenantAuthorization.canAccess(authentication, #tenantId)")
public class UserManagementController {
    private final UserManagementService service;
    private final AuditService audit;

    public UserManagementController(UserManagementService service, AuditService audit) {
        this.service = service;
        this.audit = audit;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@PathVariable UUID tenantId,
            @Valid @RequestBody CreateUserRequest request, HttpServletRequest http) {
        User user = service.create(tenantId, request.name(), request.email(), request.password(),
                request.role(), request.professionalId());
        audit.recordAuthenticated(AuditAction.USER_CREATED, tenantId, "USER", user.getId(), http);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> findAll(@PathVariable UUID tenantId,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.findAll(tenantId, pageable).map(UserResponse::from)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID tenantId, @PathVariable UUID userId) {
        return ResponseEntity.ok(UserResponse.from(service.findById(tenantId, userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID tenantId, @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request, HttpServletRequest http) {
        User user = service.update(tenantId, userId, request.name(), request.email(), request.role(), request.professionalId());
        audit.recordAuthenticated(AuditAction.USER_UPDATED, tenantId, "USER", userId, http);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID tenantId, @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserPasswordRequest request, HttpServletRequest http) {
        service.changePassword(tenantId, userId, request.newPassword());
        audit.recordAuthenticated(AuditAction.USER_PASSWORD_CHANGED, tenantId, "USER", userId, http);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID tenantId, @PathVariable UUID userId,
                                            Authentication authentication, HttpServletRequest http) {
        service.deactivate(tenantId, userId, UUID.fromString(authentication.getName()));
        audit.recordAuthenticated(AuditAction.USER_DEACTIVATED, tenantId, "USER", userId, http);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/professional/{professionalId}")
    public ResponseEntity<UserResponse> linkMyProfessional(@PathVariable UUID tenantId,
                                                            @PathVariable UUID professionalId,
                                                            Authentication authentication,
                                                            HttpServletRequest http) {
        User user = service.linkCurrentOwnerToProfessional(
                tenantId, UUID.fromString(authentication.getName()), professionalId);
        audit.recordAuthenticated(AuditAction.USER_UPDATED, tenantId, "USER", user.getId(), http);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID tenantId, @PathVariable UUID userId, HttpServletRequest http) {
        service.activate(tenantId, userId);
        audit.recordAuthenticated(AuditAction.USER_ACTIVATED, tenantId, "USER", userId, http);
        return ResponseEntity.noContent().build();
    }
}
