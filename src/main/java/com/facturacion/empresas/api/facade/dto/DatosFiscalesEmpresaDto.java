package com.facturacion.empresas.api.facade.dto;

public record DatosFiscalesEmpresaDto(
        String razonSocial,
        String nif,
        String direccion,
        String codigoPostal,
        String municipio,
        String provincia,
        String pais) {
}
