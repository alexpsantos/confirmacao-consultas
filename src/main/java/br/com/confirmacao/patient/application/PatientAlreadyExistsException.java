package br.com.confirmacao.patient.application;

public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException() { super("Já existe um paciente com este telefone nesta clínica"); }
}
