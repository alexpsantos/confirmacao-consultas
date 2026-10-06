package br.com.confirmacao.session.application;
public class SessionConflictException extends RuntimeException{public SessionConflictException(){this("Já existe uma sessão nesse horário");}public SessionConflictException(String message){super(message);}}
