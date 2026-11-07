package com.facturacion.empresas.api.facade.dto;

public record EmpresaDto(
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
