package com.facturacion.usuarios.internal.domain.exception;

public class UsuarioInvalidoException extends RuntimeException {

    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
