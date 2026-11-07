package com.facturacion.empresas.internal.domain.exception;

public class NifEmpresaDuplicadoException extends RuntimeException {

    public NifEmpresaDuplicadoException() {
        super("Ya existe otra empresa con ese NIF");
    }
}
