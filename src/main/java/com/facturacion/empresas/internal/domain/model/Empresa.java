package com.facturacion.empresas.internal.domain.model;

import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;

public record Empresa(
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

    public Empresa {
        exigir(razonSocial, "La razón social de la empresa es obligatoria");
        exigir(nif, "El NIF de la empresa es obligatorio");
        exigir(direccion, "La dirección de la empresa es obligatoria");
        exigir(codigoPostal, "El código postal de la empresa es obligatorio");
        exigir(municipio, "El municipio de la empresa es obligatorio");
        exigir(provincia, "La provincia de la empresa es obligatoria");
        exigir(pais, "El país de la empresa es obligatorio");
    }

    public static Empresa nueva(
            String razonSocial,
            String nif,
            String direccion,
            String codigoPostal,
            String municipio,
            String provincia,
            String pais,
            String email,
            String telefono) {
        return new Empresa(
                null, razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais, email, telefono, true);
    }

    public Empresa conDatosFiscales(
            String razonSocial,
            String nif,
            String direccion,
            String codigoPostal,
            String municipio,
            String provincia,
            String pais) {
        return new Empresa(
                id, razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais, email, telefono, activa);
    }

    public Empresa conContacto(String email, String telefono) {
        return new Empresa(
                id, razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais, email, telefono, activa);
    }

    private static void exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new EmpresaInvalidaException(mensaje);
        }
    }
}
