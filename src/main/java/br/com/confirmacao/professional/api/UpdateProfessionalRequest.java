package br.com.confirmacao.professional.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateProfessionalRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String fullName,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail deve ser válido")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Informe um e-mail com domínio completo, como nome@dominio.com")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "^(?=(?:\\D*\\d){10,11}\\D*$)[\\d\\s()+-]+$", message = "O telefone deve possuir 10 ou 11 números")
        String phone,

        @Size(max = 50, message = "O registro deve ter no máximo 50 caracteres")
        @Pattern(regexp = "^[\\p{L}\\d ./-]*$", message = "O registro contém caracteres inválidos")
        String registrationNumber,

        @Size(max = 100, message = "A profissão ou especialidade deve ter no máximo 100 caracteres")
        String specialty
) {
}
