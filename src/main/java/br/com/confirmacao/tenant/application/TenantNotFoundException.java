package br.com.confirmacao.tenant.application;

import java.util.UUID;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(UUID tenantId) {
        super("Clínica não encontrada: " + tenantId);
    }
}