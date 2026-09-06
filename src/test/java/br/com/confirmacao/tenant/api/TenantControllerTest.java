package br.com.confirmacao.tenant.api;

import br.com.confirmacao.shared.api.GlobalExceptionHandler;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
import br.com.confirmacao.tenant.application.TenantService;
import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCreateBody())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(
                        tenantId.toString()
                ))
                .andExpect(jsonPath("$.displayName")
                        .value("Clínica Horizonte"))
                .andExpect(jsonPath("$.timezone")
                        .value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithInvalidData()
            throws Exception {

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "displayName": "",
                                          "timezone": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.path")
                        .value(collectionUrl()))
                .andExpect(jsonPath("$.fieldErrors.displayName")
                        .value("O nome é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.timezone")
                        .value("O timezone é obrigatório"));

        verifyNoInteractions(tenantService);
    }

    @Test
    void shouldListTenants() throws Exception {
        when(tenantService.findAll())
                .thenReturn(List.of(tenant));

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(
                        tenantId.toString()
                ))
                .andExpect(jsonPath("$[0].displayName")
                        .value("Clínica Horizonte"))
                .andExpect(jsonPath("$[0].timezone")
                        .value("America/Sao_Paulo"));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoTenants()
            throws Exception {

        when(tenantService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldFindTenantById() throws Exception {
        when(tenantService.findById(tenantId))
                .thenReturn(tenant);

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        tenantId.toString()
                ))
                .andExpect(jsonPath("$.displayName")
                        .value("Clínica Horizonte"));
    }

    @Test
    void shouldReturnNotFoundWhenTenantDoesNotExist()
            throws Exception {

        when(tenantService.findById(tenantId))
                .thenThrow(new TenantNotFoundException(tenantId));

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ))
                .andExpect(jsonPath("$.path").value(itemUrl()))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validUpdateBody())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        tenantId.toString()
                ))
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
        )).thenThrow(new TenantNotFoundException(tenantId));

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(MediaType.APPLICATION_JSON)
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
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "displayName": "",
                                          "timezone": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos"))
                .andExpect(jsonPath("$.path").value(itemUrl()))
                .andExpect(jsonPath("$.fieldErrors.displayName")
                        .value("O nome é obrigatório"))
                .andExpect(jsonPath("$.fieldErrors.timezone")
                        .value("O timezone é obrigatório"));

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
        mockMvc.perform(patch(itemUrl() + "/activate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenActivatingNonexistentTenant()
            throws Exception {

        doThrow(new TenantNotFoundException(tenantId))
                .when(tenantService)
                .activate(tenantId);

        mockMvc.perform(patch(itemUrl() + "/activate"))
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