package com.facturacion.informes.internal.domain.exception;

public class InformeInvalidoException extends RuntimeException {

    public InformeInvalidoException(String mensaje) {
        super(mensaje);
    }
}
