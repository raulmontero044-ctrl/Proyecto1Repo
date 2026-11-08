package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

public enum TipoPago {
    COBRO,
    PAGO;

    public static TipoPago desde(String valor) {
        if (valor == null) {
            throw new InformeInvalidoException("El tipo del pago es obligatorio");
        }
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException excepcion) {
            throw new InformeInvalidoException("El tipo del pago debe ser COBRO o PAGO");
        }
    }
}
