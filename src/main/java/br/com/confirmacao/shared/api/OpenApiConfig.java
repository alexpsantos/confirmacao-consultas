package br.com.confirmacao.shared.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI confirmationApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "API de Confirmação de Consultas"
                                )
                                .description(
                                        "API para gerenciamento de clínicas e profissionais"
                                )
                                .version("v1")
                );
    }
}