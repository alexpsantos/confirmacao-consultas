package br.com.confirmacao.auth.application;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Não foi possível acessar sua conta. Verifique os dados informados ou entre em contato com o suporte.");
    }
}
