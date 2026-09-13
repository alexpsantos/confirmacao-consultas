package br.com.confirmacao.professional.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProfessionalAccessRequest(
        @NotBlank(message = "O nome do profissional é obrigatório") @Size(max = 150) String fullName,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") @Size(max = 254) String email,
        @Size(max = 30) String phone,
        @Size(max = 50) String registrationNumber,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password
) {}
