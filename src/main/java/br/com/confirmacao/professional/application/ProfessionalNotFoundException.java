package br.com.confirmacao.professional.application;

import java.util.UUID;

public class ProfessionalNotFoundException extends RuntimeException {

    public ProfessionalNotFoundException(UUID professionalId) {
        super("Profissional não encontrado nesta clínica: " + professionalId);
    }
}