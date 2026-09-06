package br.com.confirmacao.tenant.application;

import java.util.UUID;

public class TenantInactiveException extends RuntimeException {

    public TenantInactiveException(UUID tenantId) {
        super("A clínica está inativa: " + tenantId);
    }
}