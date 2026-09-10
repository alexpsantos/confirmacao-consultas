package br.com.confirmacao.auth.api;
import jakarta.validation.constraints.*;
public record ResetPasswordRequest(
        @NotBlank(message="O token é obrigatório") String token,
        @NotBlank(message="A nova senha é obrigatória")
        @Size(min=8,max=72,message="A senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",message="A senha deve conter letra maiúscula, minúscula e número")
        String newPassword) {}
