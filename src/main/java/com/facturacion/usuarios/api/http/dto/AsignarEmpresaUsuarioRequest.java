package com.facturacion.usuarios.api.http.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AsignarEmpresaUsuarioRequest(
        @NotNull @Pattern(regexp = "ADMINISTRADOR|GESTOR|CONSULTA") String rol) {
}
