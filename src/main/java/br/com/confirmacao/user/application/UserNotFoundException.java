package br.com.confirmacao.user.application;
import java.util.UUID;
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) { super("Usuário não encontrado: " + id); }
}
