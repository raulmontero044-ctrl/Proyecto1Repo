package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

public enum EstadoCobro {
    PENDIENTE,
    PARCIAL,
    PAGADA;

    public static EstadoCobro desde(String valor) {
        if (valor == null) {
            throw new InformeInvalidoException("El estado de la factura es obligatorio");
        }
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException excepcion) {
            throw new InformeInvalidoException("El estado de la factura debe ser PENDIENTE, PARCIAL o PAGADA");
        }
    }

    public boolean tienePendiente() {
        return this != PAGADA;
    }
}
