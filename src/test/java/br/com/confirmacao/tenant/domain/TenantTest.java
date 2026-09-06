package br.com.confirmacao.tenant.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantTest {

    @Test
    void shouldCreateTenant() {
        Tenant tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        assertNotNull(tenant.getId());
        assertEquals(
                "Clínica Horizonte",
                tenant.getDisplayName()
        );
        assertEquals(
                "America/Sao_Paulo",
                tenant.getTimezone()
        );
        assertTrue(tenant.isActive());
        assertNotNull(tenant.getCreatedAt());
        assertNotNull(tenant.getUpdatedAt());
    }

    @Test
    void shouldCreateTenantUsingJpaConstructor() {
        Tenant tenant = new Tenant();

        assertNotNull(tenant);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidDisplayName(String displayName) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Tenant(
                        displayName,
                        "America/Sao_Paulo"
                )
        );

        assertEquals(
                "O nome do tenant é obrigatório",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidTimezone(String timezone) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Tenant(
                        "Clínica Horizonte",
                        timezone
                )
        );

        assertEquals(
                "O timezone é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateTenant() {
        Tenant tenant = createTenant();
        Instant previousUpdatedAt = tenant.getUpdatedAt();

        tenant.update(
                "Clínica Atualizada",
                "America/Recife"
        );

        assertEquals(
                "Clínica Atualizada",
                tenant.getDisplayName()
        );
        assertEquals(
                "America/Recife",
                tenant.getTimezone()
        );
        assertFalse(
                tenant.getUpdatedAt().isBefore(previousUpdatedAt)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidDisplayNameWhenUpdating(
            String displayName
    ) {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenant.update(
                        displayName,
                        "America/Sao_Paulo"
                )
        );

        assertEquals(
                "O nome do tenant é obrigatório",
                exception.getMessage()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectInvalidTimezoneWhenUpdating(
            String timezone
    ) {
        Tenant tenant = createTenant();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenant.update(
                        "Clínica Atualizada",
                        timezone
                )
        );

        assertEquals(
                "O timezone é obrigatório",
                exception.getMessage()
        );
    }

    @Test
    void shouldDeactivateTenant() {
        Tenant tenant = createTenant();

        tenant.deactivate();

        assertFalse(tenant.isActive());
    }

    @Test
    void shouldActivateTenant() {
        Tenant tenant = createTenant();
        tenant.deactivate();

        tenant.activate();

        assertTrue(tenant.isActive());
    }

    private Tenant createTenant() {
        return new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );
    }

    @Test
    void shouldRejectInvalidTimezoneWhenCreatingTenant() {
        InvalidTimezoneException exception = assertThrows(
                InvalidTimezoneException.class,
                () -> new Tenant(
                        "Clínica Horizonte",
                        "Brasil/Sao_Paulo"
                )
        );

        assertEquals(
                "Timezone inválido: Brasil/Sao_Paulo",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInvalidTimezoneWhenUpdatingTenant() {
        Tenant tenant = new Tenant(
                "Clínica Horizonte",
                "America/Sao_Paulo"
        );

        InvalidTimezoneException exception = assertThrows(
                InvalidTimezoneException.class,
                () -> tenant.update(
                        "Clínica Atualizada",
                        "Timezone/Invalido"
                )
        );

        assertEquals(
                "Timezone inválido: Timezone/Invalido",
                exception.getMessage()
        );
    }

    @Test
    void shouldTrimTimezone() {
        Tenant tenant = new Tenant(
                "Clínica Horizonte",
                "  America/Sao_Paulo  "
        );

        assertEquals("America/Sao_Paulo", tenant.getTimezone());
    }
}