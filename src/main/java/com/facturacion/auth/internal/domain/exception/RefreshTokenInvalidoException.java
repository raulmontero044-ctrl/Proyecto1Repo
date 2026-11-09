package com.facturacion.auth.internal.domain.exception;

public class RefreshTokenInvalidoException extends RuntimeException {

    public RefreshTokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
