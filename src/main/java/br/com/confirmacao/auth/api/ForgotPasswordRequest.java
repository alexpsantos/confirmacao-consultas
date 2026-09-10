package br.com.confirmacao.auth.api;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record ForgotPasswordRequest(
        @NotNull(message="O tenant é obrigatório") UUID tenantId,
        @NotBlank(message="O e-mail é obrigatório") @Email(message="O e-mail deve ser válido") String email) {}
