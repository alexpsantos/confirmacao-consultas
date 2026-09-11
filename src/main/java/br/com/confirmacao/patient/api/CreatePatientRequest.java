package br.com.confirmacao.patient.api;

import br.com.confirmacao.patient.domain.PreferredContactChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank(message = "O nome do paciente é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres") String fullName,
        @PastOrPresent(message = "A data de nascimento não pode estar no futuro") LocalDate birthDate,
        @NotBlank(message = "O telefone do paciente é obrigatório")
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres") String phone,
        @Email(message = "O e-mail deve ser válido")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres") String email,
        PreferredContactChannel preferredChannel
) {}
