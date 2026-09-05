package br.com.confirmacao.professional.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfessionalRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
        String email,

        @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres")
        String phone,

        @Size(max = 50, message = "O registro deve ter no máximo 50 caracteres")
        String registrationNumber
) {
}