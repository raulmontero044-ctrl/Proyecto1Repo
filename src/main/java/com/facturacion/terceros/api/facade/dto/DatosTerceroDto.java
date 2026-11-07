package com.facturacion.terceros.api.facade.dto;

public record DatosTerceroDto(
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
        boolean esProveedor) {
}
