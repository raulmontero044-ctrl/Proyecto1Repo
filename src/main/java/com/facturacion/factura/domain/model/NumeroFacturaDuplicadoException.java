package com.facturacion.factura.domain.model;

public class NumeroFacturaDuplicadoException extends RuntimeException {

    public NumeroFacturaDuplicadoException(String numero) {
        super("Ya existe una factura con el número " + numero);
    }
}
