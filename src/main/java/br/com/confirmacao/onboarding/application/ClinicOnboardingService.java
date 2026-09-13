package br.com.confirmacao.onboarding.application;

import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.application.UserManagementService;
import br.com.confirmacao.user.domain.User;
import br.com.confirmacao.user.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicOnboardingService {
    private final TenantService tenants;
    private final UserManagementService users;

    public ClinicOnboardingService(TenantService tenants, UserManagementService users) {
        this.tenants = tenants;
        this.users = users;
    }

    @Transactional
    public ClinicOnboardingResult onboard(String clinicName, String timezone,
                                           String ownerName, String ownerEmail,
                                           String password) {
        Tenant tenant = tenants.create(clinicName, timezone);
        User owner = users.create(tenant.getId(), ownerName, ownerEmail, password,
                UserRole.OWNER, null);
        return new ClinicOnboardingResult(tenant, owner);
    }
}
