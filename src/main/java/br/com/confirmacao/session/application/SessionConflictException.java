package br.com.confirmacao.session.application;
public class SessionConflictException extends RuntimeException{public SessionConflictException(){super("Já existe uma sessão nesse horário");}}
