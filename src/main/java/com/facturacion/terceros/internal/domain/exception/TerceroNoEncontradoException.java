package com.facturacion.terceros.internal.domain.exception;

public class TerceroNoEncontradoException extends RuntimeException {

    public TerceroNoEncontradoException(Long id) {
        super("No existe el tercero con id " + id);
    }
}
