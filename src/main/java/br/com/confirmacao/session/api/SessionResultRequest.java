package br.com.confirmacao.session.api;

import br.com.confirmacao.session.domain.SessionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SessionResultRequest(
        @NotNull SessionStatus status,
        @Size(max = 500) String notes
) {}
