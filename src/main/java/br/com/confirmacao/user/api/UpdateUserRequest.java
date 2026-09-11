package br.com.confirmacao.user.api;

import br.com.confirmacao.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateUserRequest(
        @NotBlank(message = "O nome é obrigatório") @Size(max = 150) String name,
        @NotBlank(message = "O e-mail é obrigatório") @Email @Size(max = 254) String email,
        @NotNull(message = "O perfil é obrigatório") UserRole role,
        UUID professionalId
) {}
