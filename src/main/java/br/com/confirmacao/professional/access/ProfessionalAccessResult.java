package br.com.confirmacao.professional.access;

import br.com.confirmacao.professional.domain.Professional;
import br.com.confirmacao.user.domain.User;

public record ProfessionalAccessResult(Professional professional, User user) {}
