package com.facturacion.usuarios.api.http.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccesoEmpresaRequest(
        @NotNull Long empresaId,
        @NotNull @Pattern(regexp = "ADMINISTRADOR|GESTOR|CONSULTA") String rol) {
}
