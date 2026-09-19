package br.com.confirmacao.session.application;import java.util.UUID;
public class SessionNotFoundException extends RuntimeException{public SessionNotFoundException(UUID id){super("Sessão não encontrada: "+id);}}
