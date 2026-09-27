package br.com.confirmacao.whatsapp.application;
import java.util.UUID;
public class WhatsAppMessageNotFoundException extends RuntimeException { public WhatsAppMessageNotFoundException(UUID id) { super("Mensagem do WhatsApp não encontrada: " + id); } }
