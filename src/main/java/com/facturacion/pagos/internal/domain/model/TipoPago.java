package com.facturacion.pagos.internal.domain.model;

import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;

public enum TipoPago {
    COBRO,
    PAGO;

    public static TipoPago paraFacturaDeTipo(String tipoFactura) {
        if ("EMITIDA".equals(tipoFactura)) {
            return COBRO;
        }
        if ("RECIBIDA".equals(tipoFactura)) {
            return PAGO;
        }
        throw new PagoInvalidoException("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }
}
