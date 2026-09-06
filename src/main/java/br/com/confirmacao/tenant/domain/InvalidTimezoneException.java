package br.com.confirmacao.tenant.domain;

public class InvalidTimezoneException extends RuntimeException {

    public InvalidTimezoneException(String timezone) {
        super("Timezone inválido: " + timezone);
    }
}
