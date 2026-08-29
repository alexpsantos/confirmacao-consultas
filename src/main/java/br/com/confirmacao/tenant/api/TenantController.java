package br.com.confirmacao.tenant.api;


import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest request) {
        Tenant tenant = tenantService.create(
                request.displayName(),
                request.timezone()
        );

        return TenantResponse.from(tenant);
    }

    @GetMapping
    public List<TenantResponse> findAll() {
        List<Tenant> tenants = tenantService.findAll();
        List<TenantResponse> responses = new ArrayList<>();

        for (Tenant tenant : tenants) {
            TenantResponse response = TenantResponse.from(tenant);
            responses.add(response);
        }
        return responses;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> findById(@PathVariable UUID id) {
        Optional<Tenant> tenantOptional = tenantService.findById(id);

        if (tenantOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Tenant tenant = tenantOptional.get();
        TenantResponse response = TenantResponse.from(tenant);
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateTenantRequest request) {
        Optional<Tenant> tenantOptional = tenantService.update(id, request.displayName(), request.timezone());
        if (tenantOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Tenant tenant = tenantOptional.get();
        TenantResponse response = TenantResponse.from(tenant);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        boolean deactivated  = tenantService.deactivate(id);

        if (!deactivated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

}

