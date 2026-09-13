package br.com.confirmacao.onboarding.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicOnboardingRequest(
        @NotBlank(message = "O nome da clínica é obrigatório") @Size(max = 150) String clinicName,
        @NotBlank(message = "O timezone é obrigatório") @Size(max = 60) String timezone,
        @NotBlank(message = "O nome do responsável é obrigatório") @Size(max = 150) String ownerName,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "O e-mail deve ser válido") @Size(max = 254) String ownerEmail,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password
) {}
