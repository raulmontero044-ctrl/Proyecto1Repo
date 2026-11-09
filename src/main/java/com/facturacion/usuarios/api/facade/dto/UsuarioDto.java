package com.facturacion.usuarios.api.facade.dto;

import java.util.List;

public record UsuarioDto(
        Long id,
        String email,
        String nombre,
        boolean activo,
        boolean administrador,
        List<AccesoEmpresaDto> accesos) {
}
