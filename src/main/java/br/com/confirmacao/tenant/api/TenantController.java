package br.com.confirmacao.tenant.api;

import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse create(
            @Valid @RequestBody CreateTenantRequest request
    ) {
        Tenant tenant = tenantService.create(
                request.displayName(),
                request.timezone()
        );

        return TenantResponse.from(tenant);
    }

    @GetMapping
    public List<TenantResponse> findAll() {
        return tenantService.findAll()
                .stream()
                .map(TenantResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> findById(
            @PathVariable UUID id
    ) {
        Tenant tenant = tenantService.findById(id);

        TenantResponse response = TenantResponse.from(tenant);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request
    ) {
        Tenant tenant = tenantService.update(
                id,
                request.displayName(),
                request.timezone()
        );

        TenantResponse response = TenantResponse.from(tenant);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id
    ) {
        tenantService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable UUID id
    ) {
        tenantService.activate(id);

        return ResponseEntity.noContent().build();
    }
}