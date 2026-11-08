package com.facturacion.documentos.internal.domain.exception;

public class FacturaInexistenteException extends RuntimeException {

    public FacturaInexistenteException(Long facturaId) {
        super("No existe la factura con id " + facturaId);
    }
}
