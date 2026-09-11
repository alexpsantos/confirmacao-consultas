package br.com.confirmacao.patient.application;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(UUID patientId) { super("Paciente não encontrado: " + patientId); }
}
