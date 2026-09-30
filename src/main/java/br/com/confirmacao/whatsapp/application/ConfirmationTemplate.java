package br.com.confirmacao.whatsapp.application;

import java.util.UUID;

public record ConfirmationTemplate(
        UUID professionalId, String to, String patientName, String professionalName, String date, String time,
        String confirmPayload, String cancelPayload
) {}
