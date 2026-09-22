package br.com.confirmacao.professional.api;

import jakarta.validation.constraints.*;

public record UpdateMyProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Pattern(regexp = "^(?=(?:\\D*\\d){10,11}\\D*$)[\\d\\s()+-]+$") String phone,
        @Size(max = 50) @Pattern(regexp = "^[\\p{L}\\d ./-]*$") String registrationNumber,
        @Size(max = 100) String specialty,
        @Size(max = 150) String displayName,
        @NotBlank @Size(max = 60) @Pattern(regexp="^[A-Za-z_]+(?:/[A-Za-z0-9_+.-]+)+$",message="O fuso horário deve usar o formato Região/Cidade") String timezone,
        @NotNull @Min(15) @Max(240) Integer defaultSessionMinutes,
        @NotBlank @Pattern(regexp = "PRESENTIAL|ONLINE") String defaultModality,
        @NotNull Boolean remindersEnabled) {}
