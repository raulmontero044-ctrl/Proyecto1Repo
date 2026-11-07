package com.facturacion.usuarios.internal.domain.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe el usuario con id " + id);
    }
}
