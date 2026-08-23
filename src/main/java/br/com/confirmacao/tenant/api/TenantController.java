package br.com.confirmacao.tenant.api;


import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}

