package br.com.confirmacao.admin.api;

public record AdminOperationalSummaryResponse(
        long activeProfessionals,
        long activePatients,
        long sessions,
        long canceledSessions
) {}
