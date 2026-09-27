package br.com.confirmacao.whatsapp.application;
import br.com.confirmacao.session.domain.SessionAppointment;
import br.com.confirmacao.whatsapp.domain.WhatsAppMessage;
public record WhatsAppActionResult(String action, WhatsAppMessage message, SessionAppointment session, boolean processed) {}
