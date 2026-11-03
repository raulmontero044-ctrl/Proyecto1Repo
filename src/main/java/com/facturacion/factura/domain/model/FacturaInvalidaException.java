package com.facturacion.factura.domain.model;

public class FacturaInvalidaException extends RuntimeException {

    public FacturaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
