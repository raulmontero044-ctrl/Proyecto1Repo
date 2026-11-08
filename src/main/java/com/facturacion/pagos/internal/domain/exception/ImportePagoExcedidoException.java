package com.facturacion.pagos.internal.domain.exception;

import java.math.BigDecimal;

public class ImportePagoExcedidoException extends RuntimeException {

    public ImportePagoExcedidoException(Long facturaId, BigDecimal pendiente) {
        super("El importe del pago supera el pendiente de la factura con id " + facturaId
                + " (pendiente: " + pendiente.toPlainString() + ")");
    }
}
