package com.facturacion.usuarios.api.facade.dto;

import java.util.List;

public record NuevoUsuarioDto(
        String email,
        String nombre,
        String password,
        List<AccesoEmpresaDto> accesos) {
}
