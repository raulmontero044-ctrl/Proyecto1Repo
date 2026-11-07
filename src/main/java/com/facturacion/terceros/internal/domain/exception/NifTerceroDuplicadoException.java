package com.facturacion.terceros.internal.domain.exception;

public class NifTerceroDuplicadoException extends RuntimeException {

    public NifTerceroDuplicadoException() {
        super("Ya existe otro tercero con ese NIF en la empresa");
    }
}
