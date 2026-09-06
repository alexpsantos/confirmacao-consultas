package br.com.confirmacao;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ConfirmacaoConsultasApplicationTest {

    @Test
    void shouldRunApplication() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {

            ConfirmacaoConsultasApplication.main(args);

            springApplication.verify(() ->
                    SpringApplication.run(
                            ConfirmacaoConsultasApplication.class,
                            args
                    )
            );
        }
    }
}
