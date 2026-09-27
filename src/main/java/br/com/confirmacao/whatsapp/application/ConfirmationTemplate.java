package br.com.confirmacao.whatsapp.application;

public record ConfirmationTemplate(
        String to, String patientName, String professionalName, String date, String time,
        String confirmPayload, String cancelPayload
) {}
