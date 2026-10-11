package br.com.confirmacao.professional.location.api;
import jakarta.validation.constraints.*;
public record ProfessionalLocationRequest(@NotBlank @Size(max=100)String city,@NotBlank @Pattern(regexp="^[A-Za-z]{2}$")String state,@NotBlank @Size(max=60) @Pattern(regexp="^[A-Za-z_]+(?:/[A-Za-z0-9_+.-]+)+$")String timezone){}
