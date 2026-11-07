package com.facturacion.empresas.internal.domain.exception;

public class EmpresaInvalidaException extends RuntimeException {

    public EmpresaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
