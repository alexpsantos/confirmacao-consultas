package br.com.confirmacao.professional.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProfessionalAlreadyExistsException extends RuntimeException {

    public ProfessionalAlreadyExistsException(String message) {
        super(message);
    }
}