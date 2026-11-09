package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

public enum TipoFactura {
    EMITIDA,
    RECIBIDA;

    public static TipoFactura desde(String valor) {
        if (valor == null) {
            throw new InformeInvalidoException("El tipo de la factura es obligatorio");
        }
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException excepcion) {
            throw new InformeInvalidoException("El tipo de la factura debe ser EMITIDA o RECIBIDA");
        }
    }
}
