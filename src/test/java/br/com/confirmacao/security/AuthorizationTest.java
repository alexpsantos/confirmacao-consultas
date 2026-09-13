package br.com.confirmacao.security;

import br.com.confirmacao.professional.api.ProfessionalController;
import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.audit.api.AuditLogController;
import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.access.ProfessionalAccessService;
import br.com.confirmacao.tenant.api.TenantController;
import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        TenantController.class,
        ProfessionalController.class,
        AuditLogController.class
})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@Import({
        SecurityConfig.class,
        TenantAuthorization.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
@TestPropertySource(properties = {
        "app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "app.security.jwt.issuer=http://localhost:8080"
})
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private ProfessionalService professionalService;

    @MockitoBean
    private ProfessionalAccessService professionalAccessService;

    @MockitoBean
    private AuditService auditService;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Autenticação necessária"));

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldAllowAdminToListTenants() throws Exception {
        when(tenantService.findAll(eq(null), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/tenants").with(role("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidOwnerFromListingAllTenants() throws Exception {
        mockMvc.perform(get("/api/v1/tenants").with(role("OWNER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(
                        "Você não possui permissão para esta operação"
                ));

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldAllowOwnerToFindTenantById() throws Exception {
        Tenant tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );
        when(tenantService.findById(tenant.getId())).thenReturn(tenant);

        mockMvc.perform(
                        get("/api/v1/tenants/{id}", tenant.getId())
                                .with(role("OWNER", tenant.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(tenant.getId().toString()));
    }

    @Test
    void shouldForbidOwnerFromAccessingAnotherTenant() throws Exception {
        UUID requestedTenantId = UUID.randomUUID();
        UUID authenticatedTenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/tenants/{id}", requestedTenantId)
                                .with(role("OWNER", authenticatedTenantId))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldForbidOwnerFromDeactivatingTenant() throws Exception {
        mockMvc.perform(
                        delete("/api/v1/tenants/{id}", UUID.randomUUID())
                                .with(role("OWNER"))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldAllowAdminToActivateTenant() throws Exception {
        mockMvc.perform(
                        patch("/api/v1/tenants/{id}/activate", UUID.randomUUID())
                                .with(role("ADMIN"))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldForbidProfessionalFromListingProfessionals() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/v1/tenants/{tenantId}/professionals", tenantId)
                                .with(role("PROFESSIONAL", tenantId))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidProfessionalFromReadingAnotherTenant() throws Exception {
        UUID requestedTenantId = UUID.randomUUID();
        UUID authenticatedTenantId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/v1/tenants/{tenantId}/professionals",
                                requestedTenantId
                        ).with(role("PROFESSIONAL", authenticatedTenantId))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(professionalService);
    }

    @Test
    void shouldForbidProfessionalFromCreatingProfessional() throws Exception {
        mockMvc.perform(
                        post("/api/v1/tenants/{tenantId}/professionals", UUID.randomUUID())
                                .with(role("PROFESSIONAL"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(professionalService);
    }

    @Test
    void shouldAllowOwnerToActivateProfessional() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/api/v1/tenants/{tenantId}/professionals/{professionalId}/activate",
                                tenantId,
                                UUID.randomUUID()
                        ).with(role("OWNER", tenantId))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowOwnerToReadOwnAuditLogs() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(auditService.findAll(eq(tenantId), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit-logs", tenantId)
                        .with(role("OWNER", tenantId)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidOwnerFromReadingAnotherTenantAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit-logs", UUID.randomUUID())
                        .with(role("OWNER", UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldForbidProfessionalFromReadingAuditLogs() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/tenants/{tenantId}/audit-logs", tenantId)
                        .with(role("PROFESSIONAL", tenantId)))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor role(String role) {
        return jwt().authorities(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    private static RequestPostProcessor role(
            String role,
            UUID tenantId
    ) {
        return jwt()
                .jwt(jwt -> jwt.claim("tenant_id", tenantId.toString()))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );
    }
}
