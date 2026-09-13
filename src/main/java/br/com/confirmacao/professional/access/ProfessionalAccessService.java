package br.com.confirmacao.professional.access;

import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.user.application.UserManagementService;
import br.com.confirmacao.user.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfessionalAccessService {
    private final ProfessionalService professionals;
    private final UserManagementService users;

    public ProfessionalAccessService(ProfessionalService professionals, UserManagementService users) {
        this.professionals = professionals;
        this.users = users;
    }

    @Transactional
    public ProfessionalAccessResult create(UUID tenantId, String fullName, String email, String phone,
                                           String registrationNumber, String password) {
        Professional professional = professionals.create(
                tenantId, fullName, email, phone, registrationNumber);
        var user = users.create(tenantId, fullName, email, password,
                UserRole.PROFESSIONAL, professional.getId());
        return new ProfessionalAccessResult(professional, user);
    }
}
