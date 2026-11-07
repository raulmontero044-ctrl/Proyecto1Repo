package com.facturacion.terceros.internal.domain.model;

import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;

public record Tercero(
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

    public Tercero {
        if (empresaId == null) {
            throw new TerceroInvalidoException("La empresa del tercero es obligatoria");
        }
        exigir(nif, "El NIF del tercero es obligatorio");
        exigir(razonSocial, "La razón social del tercero es obligatoria");
        if (!esCliente && !esProveedor) {
            throw new TerceroInvalidoException("El tercero debe ser cliente, proveedor o ambos");
        }
    }

    public static Tercero nuevo(
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
            boolean esProveedor) {
        return new Tercero(
                null, empresaId, nif, razonSocial, direccion, codigoPostal, municipio, provincia, pais,
                email, telefono, esCliente, esProveedor, true);
    }

    public Tercero conDatos(
            String nuevoNif,
            String nuevaRazonSocial,
            String nuevaDireccion,
            String nuevoCodigoPostal,
            String nuevoMunicipio,
            String nuevaProvincia,
            String nuevoPais,
            String nuevoEmail,
            String nuevoTelefono,
            boolean nuevoEsCliente,
            boolean nuevoEsProveedor) {
        return new Tercero(
                id, empresaId, nuevoNif, nuevaRazonSocial, nuevaDireccion, nuevoCodigoPostal, nuevoMunicipio,
                nuevaProvincia, nuevoPais, nuevoEmail, nuevoTelefono, nuevoEsCliente, nuevoEsProveedor, activo);
    }

    public Tercero desactivado() {
        return new Tercero(
                id, empresaId, nif, razonSocial, direccion, codigoPostal, municipio, provincia, pais,
                email, telefono, esCliente, esProveedor, false);
    }

    private static void exigir(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new TerceroInvalidoException(mensaje);
        }
    }
}
