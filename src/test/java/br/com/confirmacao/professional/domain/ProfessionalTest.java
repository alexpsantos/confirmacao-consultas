package br.com.confirmacao.professional.domain;

import br.com.confirmacao.tenant.domain.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfessionalTest {

    private final Tenant tenant =
            new Tenant("Clínica Horizonte", "America/Sao_Paulo");

    @Test
    void shouldCreateProfessional() {
        Professional professional = new Professional(
                tenant,
                "  Ana Souza  ",
                "  ANA@EXEMPLO.COM  ",
                "11999999999",
                "CRP 06/123456"
        );

        assertNotNull(professional.getId());
        assertSame(tenant, professional.getTenant());
        assertEquals("Ana Souza", professional.getFullName());
        assertEquals("ana@exemplo.com", professional.getEmail());
        assertEquals("11999999999", professional.getPhone());
        assertEquals(
                "CRP 06/123456",
                professional.getRegistrationNumber()
        );
        assertTrue(professional.isActive());
        assertNotNull(professional.getCreatedAt());
        assertNotNull(professional.getUpdatedAt());
    }



    @Test
    void shouldRejectNullTenant() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Professional(
                        null,
                        "Ana Souza",
                        "ana@exemplo.com",
                        null,
                        null
                )
        );

        assertEquals(
                "O tenant é obrigatório",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidFullName(String fullName) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Professional(
                        tenant,
                        fullName,
                        "ana@exemplo.com",
                        null,
                        null
                )
        );

        assertEquals(
                "O nome do profissional é obrigatório",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidEmail(String email) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Professional(
                        tenant,
                        "Ana Souza",
                        email,
                        null,
                        null
                )
        );

        assertEquals(
                "O e-mail do profissional é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateProfessional() {
        Professional professional = createProfessional();
        Instant previousUpdatedAt = professional.getUpdatedAt();

        professional.update(
                "  Ana Atualizada  ",
                "  NOVO@EXEMPLO.COM  ",
                "11988887777",
                "CRP 06/654321"
        );

        assertEquals(
                "Ana Atualizada",
                professional.getFullName()
        );
        assertEquals(
                "novo@exemplo.com",
                professional.getEmail()
        );
        assertEquals(
                "11988887777",
                professional.getPhone()
        );
        assertEquals(
                "CRP 06/654321",
                professional.getRegistrationNumber()
        );
        assertFalse(
                professional.getUpdatedAt().isBefore(previousUpdatedAt)
        );
    }

    @Test
    void shouldRejectInvalidNameWhenUpdating() {
        Professional professional = createProfessional();

        assertThrows(
                IllegalArgumentException.class,
                () -> professional.update(
                        " ",
                        "novo@exemplo.com",
                        null,
                        null
                )
        );
    }

    @Test
    void shouldRejectInvalidEmailWhenUpdating() {
        Professional professional = createProfessional();

        assertThrows(
                IllegalArgumentException.class,
                () -> professional.update(
                        "Ana Souza",
                        " ",
                        null,
                        null
                )
        );
    }

    @Test
    void shouldDeactivateProfessional() {
        Professional professional = createProfessional();

        professional.deactivate();

        assertFalse(professional.isActive());
    }

    @Test
    void shouldActivateProfessional() {
        Professional professional = createProfessional();
        professional.deactivate();

        professional.activate();

        assertTrue(professional.isActive());
    }

    private Professional createProfessional() {
        return new Professional(
                tenant,
                "Ana Souza",
                "ana@exemplo.com",
                "11999999999",
                "CRP 06/123456"
        );
    }

    @Test
    void shouldCreateProfessionalUsingJpaConstructor() {
        Professional professional = new Professional();

        assertNotNull(professional);
    }
}