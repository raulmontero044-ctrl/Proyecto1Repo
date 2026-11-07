package com.facturacion.usuarios.internal.domain.exception;

public class EmailUsuarioDuplicadoException extends RuntimeException {

    public EmailUsuarioDuplicadoException() {
        super("Ya existe otro usuario con ese email");
    }
}
