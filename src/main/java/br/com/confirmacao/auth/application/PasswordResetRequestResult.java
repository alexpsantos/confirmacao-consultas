package br.com.confirmacao.auth.application;
import br.com.confirmacao.user.domain.User;
public record PasswordResetRequestResult(String token, User user) {}
