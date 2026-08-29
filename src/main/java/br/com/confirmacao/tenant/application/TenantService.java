package br.com.confirmacao.tenant.application;


import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.tenant.infrastructure.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public Tenant create(String displayName, String timezone){
        Tenant tenant = new Tenant(displayName, timezone);
        return tenantRepository.save(tenant);
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll(){
        return tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tenant> findById(UUID id) {
        return tenantRepository.findById(id);
    }

    @Transactional
    public Optional<Tenant> update(UUID id, String displayName, String timezone) {
        Optional<Tenant> tenantOptional = tenantRepository.findById(id);

        if (tenantOptional.isEmpty()) {
            return Optional.empty();
        }

        Tenant tenant = tenantOptional.get();
        tenant.update(displayName, timezone);

        return Optional.of(tenant);
    }


}
