package com.facturacion.terceros.api.http.dto;

public record TerceroResponse(
        Long id,
        Long empresaId,
        String nif,
        String razonSocial,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        String pais,
        String email,
        String telefono,
        boolean esCliente,
        boolean esProveedor,
        boolean activo) {
}
