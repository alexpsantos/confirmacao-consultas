package br.com.confirmacao.auth.application;
public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() { super("Token de recuperação inválido ou expirado"); }
}
