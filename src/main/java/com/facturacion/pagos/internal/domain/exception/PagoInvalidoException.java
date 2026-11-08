package com.facturacion.pagos.internal.domain.exception;

public class PagoInvalidoException extends RuntimeException {

    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
