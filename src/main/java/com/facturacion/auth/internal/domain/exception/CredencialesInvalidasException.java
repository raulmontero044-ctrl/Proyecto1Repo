package com.facturacion.auth.internal.domain.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("El email o la contraseña no son correctos");
    }
}
