package br.com.confirmacao.auth.api;import jakarta.validation.constraints.*;public record ForgotPasswordRequest(@NotBlank@Email String email){}
