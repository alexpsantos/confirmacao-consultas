package br.com.confirmacao.onboarding.application;

import br.com.confirmacao.tenant.domain.Tenant;
import br.com.confirmacao.user.domain.User;

public record ClinicOnboardingResult(Tenant tenant, User owner) {}
