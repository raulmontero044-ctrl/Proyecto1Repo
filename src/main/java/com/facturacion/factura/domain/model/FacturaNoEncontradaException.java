package com.facturacion.factura.domain.model;

public class FacturaNoEncontradaException extends RuntimeException {

    public FacturaNoEncontradaException(Long id) {
        super("No existe la factura con id " + id);
    }
}
