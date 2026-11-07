package com.facturacion.terceros.internal.domain.exception;

public class TerceroInvalidoException extends RuntimeException {

    public TerceroInvalidoException(String mensaje) {
        super(mensaje);
    }
}
