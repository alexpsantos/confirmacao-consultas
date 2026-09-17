package br.com.confirmacao.professional.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record CreateProfessionalAccessRequest(
        @NotBlank(message = "O nome do profissional é obrigatório") @Size(max = 150) String fullName,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Informe um e-mail com domínio completo, como nome@dominio.com") @Size(max = 254) String email,
        @NotBlank(message = "O telefone é obrigatório") @Pattern(regexp = "^(?=(?:\\D*\\d){10,11}\\D*$)[\\d\\s()+-]+$", message = "O telefone deve possuir 10 ou 11 números") String phone,
        @Size(max = 50, message = "O registro deve ter no máximo 50 caracteres") @Pattern(regexp = "^[\\p{L}\\d ./-]*$", message = "O registro contém caracteres inválidos") String registrationNumber,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password
) {}
