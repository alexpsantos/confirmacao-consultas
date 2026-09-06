package br.com.confirmacao.shared.api;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    @Test
    void shouldConfigureOpenApiInformation() {
        OpenAPI openAPI = new OpenApiConfig().confirmationApi();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals(
                "API de Confirmação de Consultas",
                openAPI.getInfo().getTitle()
        );
        assertEquals(
                "API para gerenciamento de clínicas e profissionais",
                openAPI.getInfo().getDescription()
        );
        assertEquals("v1", openAPI.getInfo().getVersion());
    }
}
