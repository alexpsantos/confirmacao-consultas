package br.com.confirmacao.tenant.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record  CreateTenantRequest(


    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String displayName,

    @NotBlank(message = "O timezone é obrigatório")
    @Size(max = 60, message = "O timezone deve ter no máximo 60 caracteres")
    String timezone
){
}

