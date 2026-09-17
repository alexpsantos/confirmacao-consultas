package br.com.confirmacao.patient.api;

import br.com.confirmacao.patient.domain.PreferredContactChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UpdatePatientRequest(
        @NotBlank(message = "O nome do paciente é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres") String fullName,
        @PastOrPresent(message = "A data de nascimento não pode estar no futuro") LocalDate birthDate,
        @NotBlank(message = "O telefone do paciente é obrigatório")
        @Pattern(regexp = "^(?=(?:\\D*\\d){10,11}\\D*$)[\\d\\s()+-]+$", message = "O telefone deve possuir 10 ou 11 números") String phone,
        @Email(message = "O e-mail deve ser válido")
        @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Informe um e-mail com domínio completo, como nome@dominio.com")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres") String email,
        @NotNull(message = "O canal preferencial é obrigatório") PreferredContactChannel preferredChannel
) {}
