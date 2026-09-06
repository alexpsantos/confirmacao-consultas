package br.com.confirmacao.tenant.application;

import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant create(
            String displayName,
            String timezone
    ) {
        Tenant tenant = new Tenant(displayName, timezone);
        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll(Boolean active) {
        if (active == null) {
            return tenantRepository.findAll();
        }

        return tenantRepository.findAllByActive(active);
    }

    @Transactional(readOnly = true)
    public Tenant findById(UUID id) {
        return findTenantOrThrow(id);
    }

    @Transactional
    public Tenant update(
            UUID id,
            String displayName,
            String timezone
    ) {
        Tenant tenant = findTenantOrThrow(id);

        tenant.update(displayName, timezone);

        return tenant;
    }

    @Transactional
    public void deactivate(UUID id) {
        Tenant tenant = findTenantOrThrow(id);
        tenant.deactivate();
    }

    @Transactional
    public void activate(UUID id) {
        Tenant tenant = findTenantOrThrow(id);
        tenant.activate();
    }

    private Tenant findTenantOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new TenantNotFoundException(id)
                );
    }
}