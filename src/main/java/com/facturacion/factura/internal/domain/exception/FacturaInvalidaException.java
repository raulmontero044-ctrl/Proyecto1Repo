package com.facturacion.factura.internal.domain.exception;

public class FacturaInvalidaException extends RuntimeException {

    public FacturaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
