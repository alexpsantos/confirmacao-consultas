package br.com.confirmacao.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SelectTenantRequest(
        @NotBlank(message = "O token de seleção é obrigatório") String selectionToken,
        @NotNull(message = "A clínica é obrigatória") UUID tenantId
) {}
