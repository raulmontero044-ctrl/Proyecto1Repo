package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;

public enum EntidadTipo {
    FACTURA,
    TERCERO,
    EMPRESA,
    PAGO;

    public static EntidadTipo desde(String valor) {
        if (valor == null) {
            throw new DocumentoInvalidoException("El tipo de entidad del documento es obligatorio");
        }
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException excepcion) {
            throw new DocumentoInvalidoException(
                    "El tipo de entidad del documento debe ser FACTURA, TERCERO, EMPRESA o PAGO");
        }
    }
}
