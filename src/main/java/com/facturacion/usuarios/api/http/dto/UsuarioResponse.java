package com.facturacion.usuarios.api.http.dto;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        boolean activo,
        List<AccesoEmpresaResponse> accesos) {
}
