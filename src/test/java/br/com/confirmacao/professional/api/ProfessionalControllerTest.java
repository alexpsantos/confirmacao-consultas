package br.com.confirmacao.professional.api;

import br.com.confirmacao.professional.application.ProfessionalAlreadyExistsException;
import br.com.confirmacao.professional.application.ProfessionalNotFoundException;
import br.com.confirmacao.professional.application.ProfessionalService;
import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.shared.api.GlobalExceptionHandler;
import br.com.confirmacao.tenant.application.TenantNotFoundException;
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

@WebMvcTest(ProfessionalController.class)
@Import(GlobalExceptionHandler.class)
class ProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessionalService professionalService;

    private UUID tenantId;
    private UUID professionalId;
    private Professional professional;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        tenantId = tenant.getId();

        professional = new Professional(
                tenant,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );

        professionalId = professional.getId();
    }

    @Test
    void shouldCreateProfessional() throws Exception {
        when(professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        )).thenReturn(professional);

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCreateBody())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(
                        professionalId.toString()
                ))
                .andExpect(jsonPath("$.tenantId").value(
                        tenantId.toString()
                ))
                .andExpect(jsonPath("$.fullName").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.phone").value("11999999999"))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("CRP 06/123456"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingForNonexistentTenant()
            throws Exception {

        when(professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        )).thenThrow(new TenantNotFoundException(tenantId));

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCreateBody())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ))
                .andExpect(jsonPath("$.path").value(collectionUrl()))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingWithInvalidData()
            throws Exception {

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "fullName": "",
                                          "email": "email-invalido",
                                          "phone": "11999999999",
                                          "registrationNumber": "CRP 06/123456"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(professionalService);
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateProfessional()
            throws Exception {

        when(professionalService.create(
                tenantId,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        )).thenThrow(
                new ProfessionalAlreadyExistsException(
                        "Já existe um profissional com este e-mail nesta clínica"
                )
        );

        mockMvc.perform(
                        post(collectionUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validCreateBody())
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldListProfessionals() throws Exception {
        when(professionalService.findAll(tenantId))
                .thenReturn(List.of(professional));

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(
                        professionalId.toString()
                ))
                .andExpect(jsonPath("$[0].tenantId").value(
                        tenantId.toString()
                ))
                .andExpect(jsonPath("$[0].fullName").value("Ana Souza"));
    }

    @Test
    void shouldReturnEmptyListWhenTenantHasNoProfessionals()
            throws Exception {

        when(professionalService.findAll(tenantId))
                .thenReturn(List.of());

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenListingNonexistentTenant()
            throws Exception {

        when(professionalService.findAll(tenantId))
                .thenThrow(new TenantNotFoundException(tenantId));

        mockMvc.perform(get(collectionUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "Clínica não encontrada: " + tenantId
                ));
    }

    @Test
    void shouldFindProfessionalById() throws Exception {
        when(professionalService.findById(
                tenantId,
                professionalId
        )).thenReturn(professional);

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(
                        professionalId.toString()
                ))
                .andExpect(jsonPath("$.fullName").value("Ana Souza"));
    }

    @Test
    void shouldReturnNotFoundWhenProfessionalDoesNotExist()
            throws Exception {

        when(professionalService.findById(
                tenantId,
                professionalId
        )).thenThrow(
                new ProfessionalNotFoundException(professionalId)
        );

        mockMvc.perform(get(itemUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Profissional não encontrado nesta clínica: "
                                + professionalId
                ))
                .andExpect(jsonPath("$.path").value(itemUrl()));
    }

    @Test
    void shouldUpdateProfessional() throws Exception {
        professional.update(
                "Ana Atualizada",
                "atualizada@exemplo.com",
                "11988887777",
                "CRP 06/654321"
        );

        when(professionalService.update(
                tenantId,
                professionalId,
                "Ana Atualizada",
                "atualizada@exemplo.com",
                "11988887777",
                "CRP 06/654321"
        )).thenReturn(professional);

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validUpdateBody())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName")
                        .value("Ana Atualizada"))
                .andExpect(jsonPath("$.email")
                        .value("atualizada@exemplo.com"))
                .andExpect(jsonPath("$.phone")
                        .value("11988887777"))
                .andExpect(jsonPath("$.registrationNumber")
                        .value("CRP 06/654321"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentProfessional()
            throws Exception {

        when(professionalService.update(
                tenantId,
                professionalId,
                "Ana Atualizada",
                "atualizada@exemplo.com",
                "11988887777",
                "CRP 06/654321"
        )).thenThrow(
                new ProfessionalNotFoundException(professionalId)
        );

        mockMvc.perform(
                        put(itemUrl())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validUpdateBody())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Profissional não encontrado nesta clínica: "
                                + professionalId
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
                                          "fullName": "",
                                          "email": "email-invalido",
                                          "phone": "11988887777",
                                          "registrationNumber": "CRP 06/654321"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(professionalService);
    }

    @Test
    void shouldDeactivateProfessional() throws Exception {
        mockMvc.perform(delete(itemUrl()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingNonexistentProfessional()
            throws Exception {

        doThrow(new ProfessionalNotFoundException(professionalId))
                .when(professionalService)
                .deactivate(tenantId, professionalId);

        mockMvc.perform(delete(itemUrl()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Profissional não encontrado nesta clínica: "
                                + professionalId
                ));
    }

    @Test
    void shouldActivateProfessional() throws Exception {
        mockMvc.perform(patch(itemUrl() + "/activate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenActivatingNonexistentProfessional()
            throws Exception {

        doThrow(new ProfessionalNotFoundException(professionalId))
                .when(professionalService)
                .activate(tenantId, professionalId);

        mockMvc.perform(patch(itemUrl() + "/activate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Profissional não encontrado nesta clínica: "
                                + professionalId
                ));
    }

    private String collectionUrl() {
        return "/api/v1/tenants/" + tenantId + "/professionals";
    }

    private String itemUrl() {
        return collectionUrl() + "/" + professionalId;
    }

    private String validCreateBody() {
        return """
                {
                  "fullName": "Ana Souza",
                  "email": "ana@exemplo.com",
                  "phone": "11999999999",
                  "registrationNumber": "CRP 06/123456"
                }
                """;
    }

    private String validUpdateBody() {
        return """
                {
                  "fullName": "Ana Atualizada",
                  "email": "atualizada@exemplo.com",
                  "phone": "11988887777",
                  "registrationNumber": "CRP 06/654321"
                }
                """;
    }
}