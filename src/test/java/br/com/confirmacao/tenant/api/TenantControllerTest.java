package br.com.confirmacao.tenant.api;

import br.com.confirmacao.shared.api.GlobalExceptionHandler;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.audit.application.AuditService;
import br.com.confirmacao.tenant.domain.InvalidTimezoneException;
import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TenantController.class)
@Import(GlobalExceptionHandler.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private AuditService auditService;

    private UUID tenantId;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        tenantId = tenant.getId();
    }

    @Test
    void shouldCreateTenant() throws Exception {
        when(tenantService.create(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        )).thenReturn(tenant);

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validCreateBody())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(tenantId.toString()))
                .andExpect(jsonPath("$.displayName")
                        .value("Clínica Horizonte"))
                .andExpect(jsonPath("$.timezone")
                        .value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.active")
                        .value(true));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithInvalidData()
            throws Exception {

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "displayName": "",
                                          "timezone": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.path")
                        .value(collectionUrl()))
                .andExpect(jsonPath(
                        "$.fieldErrors.displayName"
                ).value("O nome é obrigatório"))
                .andExpect(jsonPath(
                        "$.fieldErrors.timezone"
                ).value("O timezone é obrigatório"));

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldReturnBadRequestWhenTimezoneIsInvalid()
            throws Exception {

        when(tenantService.create(
                "Clínica Horizonte",
                "Brasil/Sao_Paulo"
        )).thenThrow(
                new InvalidTimezoneException(
                        "Brasil/Sao_Paulo"
                )
        );

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "displayName": "Clínica Horizonte",
                                          "timezone": "Brasil/Sao_Paulo"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.path")
                        .value(collectionUrl()))
                .andExpect(jsonPath(
                        "$.fieldErrors.timezone"
                ).value(
                        "Timezone inválido: Brasil/Sao_Paulo"
                ));
    }

    @Test
    void shouldListTenantsWithPagination()
            throws Exception {

        Pageable pageable = PageRequest.of(0, 2);

        Page<Tenant> result = new PageImpl<>(
                List.of(tenant),
                pageable,
                1
        );

        when(tenantService.findAll(
                isNull(),
                any(Pageable.class)
        )).thenReturn(result);

        mockMvc.perform(
                        get(collectionUrl())
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content.length()"
                ).value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(tenantId.toString()))
                .andExpect(jsonPath(
                        "$.content[0].displayName"
                ).value("Clínica Horizonte"))
                .andExpect(jsonPath(
                        "$.content[0].timezone"
                ).value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath(
                        "$.totalElements"
                ).value(1))
                .andExpect(jsonPath(
                        "$.totalPages"
                ).value(1))
                .andExpect(jsonPath("$.first")
                        .value(true))
                .andExpect(jsonPath("$.last")
                        .value(true));

        verify(tenantService).findAll(
                isNull(),
                argThat(page ->
                        page.getPageNumber() == 0
                                && page.getPageSize() == 2
                )
        );
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoTenants()
            throws Exception {

        when(tenantService.findAll(
                isNull(),
                any(Pageable.class)
        )).thenReturn(
                Page.empty(PageRequest.of(0, 20))
        );

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isEmpty())
                .andExpect(jsonPath("$.page")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(20))
                .andExpect(jsonPath(
                        "$.totalElements"
                ).value(0))
                .andExpect(jsonPath(
                        "$.totalPages"
                ).value(0))
                .andExpect(jsonPath("$.first")
                        .value(true))
                .andExpect(jsonPath("$.last")
                        .value(true));
    }

    @Test
    void shouldListOnlyActiveTenants()
            throws Exception {

        Tenant activeTenant = new Tenant(
                "Clínica Ativa",
                "America/Sao_Paulo"
        );

        Page<Tenant> result = new PageImpl<>(
                List.of(activeTenant),
                PageRequest.of(1, 2),
                5
        );

        when(tenantService.findAll(
                eq(true),
                any(Pageable.class)
        )).thenReturn(result);

        mockMvc.perform(
                        get(collectionUrl())
                                .param("active", "true")
                                .param("page", "1")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content.length()"
                ).value(1))
                .andExpect(jsonPath(
                        "$.content[0].displayName"
                ).value("Clínica Ativa"))
                .andExpect(jsonPath(
                        "$.content[0].active"
                ).value(true))
                .andExpect(jsonPath("$.page")
                        .value(1))
                .andExpect(jsonPath("$.size")
                        .value(2))
                .andExpect(jsonPath(
                        "$.totalElements"
                ).value(5))
                .andExpect(jsonPath(
                        "$.totalPages"
                ).value(3));

        verify(tenantService).findAll(
                eq(true),
                argThat(page ->
                        page.getPageNumber() == 1
                                && page.getPageSize() == 2
                )
        );
    }

    @Test
    void shouldListOnlyInactiveTenants()
            throws Exception {

        Tenant inactiveTenant = new Tenant(
                "Clínica Inativa",
                "America/Sao_Paulo"
        );
        inactiveTenant.deactivate();

        Page<Tenant> result = new PageImpl<>(
                List.of(inactiveTenant),
                PageRequest.of(0, 2),
                1
        );

        when(tenantService.findAll(
                eq(false),
                any(Pageable.class)
        )).thenReturn(result);

        mockMvc.perform(
                        get(collectionUrl())
                                .param("active", "false")
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content.length()"
                ).value(1))
                .andExpect(jsonPath(
                        "$.content[0].displayName"
                ).value("Clínica Inativa"))
                .andExpect(jsonPath(
                        "$.content[0].active"
                ).value(false));

        verify(tenantService).findAll(
                eq(false),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFindTenantById() throws Exception {
        when(tenantService.findById(tenantId))
                .thenReturn(tenant);

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(tenantId.toString()))
                .andExpect(jsonPath("$.displayName")
                        .value("Clínica Horizonte"));
    }

    @Test
    void shouldReturnNotFoundWhenTenantDoesNotExist()
            throws Exception {

        when(tenantService.findById(tenantId))
                .thenThrow(
                        new TenantNotFoundException(tenantId)
                );

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ))
                .andExpect(jsonPath("$.path")
                        .value(itemUrl()))
                .andExpect(jsonPath("$.fieldErrors")
                        .isEmpty());
    }

    @Test
    void shouldUpdateTenant() throws Exception {
        tenant.update(
                "Clínica Atualizada",
                "America/Recife"
        );

        when(tenantService.update(
                tenantId,
                "Clínica Atualizada",
                "America/Recife"
        )).thenReturn(tenant);

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validUpdateBody())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(tenantId.toString()))
                .andExpect(jsonPath("$.displayName")
                        .value("Clínica Atualizada"))
                .andExpect(jsonPath("$.timezone")
                        .value("America/Recife"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentTenant()
            throws Exception {

        when(tenantService.update(
                tenantId,
                "Clínica Atualizada",
                "America/Recife"
        )).thenThrow(
                new TenantNotFoundException(tenantId)
        );

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(validUpdateBody())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData()
            throws Exception {

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "displayName": "",
                                          "timezone": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.path")
                        .value(itemUrl()))
                .andExpect(jsonPath(
                        "$.fieldErrors.displayName"
                ).value("O nome é obrigatório"))
                .andExpect(jsonPath(
                        "$.fieldErrors.timezone"
                ).value("O timezone é obrigatório"));

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldDeactivateTenant() throws Exception {
        mockMvc.perform(delete(itemUrl()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingNonexistentTenant()
            throws Exception {

        doThrow(new TenantNotFoundException(tenantId))
                .when(tenantService)
                .deactivate(tenantId);

        mockMvc.perform(delete(itemUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ));
    }

    @Test
    void shouldActivateTenant() throws Exception {
        mockMvc.perform(
                        patch(itemUrl() + "/activate")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenActivatingNonexistentTenant()
            throws Exception {

        doThrow(new TenantNotFoundException(tenantId))
                .when(tenantService)
                .activate(tenantId);

        mockMvc.perform(
                        patch(itemUrl() + "/activate")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ));
    }

    private String collectionUrl() {
        return "/api/v1/tenants";
    }

    private String itemUrl() {
        return collectionUrl() + "/" + tenantId;
    }

    private String validCreateBody() {
        return """
                {
                  "displayName": "Clínica Horizonte",
                  "timezone": "America/Sao_Paulo"
                }
                """;
    }

    private String validUpdateBody() {
        return """
                {
                  "displayName": "Clínica Atualizada",
                  "timezone": "America/Recife"
                }
                """;
    }
}
