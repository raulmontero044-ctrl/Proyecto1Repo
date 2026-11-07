package com.facturacion.empresas.api.http.dto;

public record EmpresaResponse(
        Long id,
        String razonSocial,
        String nif,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        String pais,
        String email,
        String telefono,
        boolean activa) {
}
