package com.facturacion.factura.internal.domain.exception;

public class FacturaNoEncontradaException extends RuntimeException {

    public FacturaNoEncontradaException(Long id) {
        super("No existe la factura con id " + id);
    }
}
