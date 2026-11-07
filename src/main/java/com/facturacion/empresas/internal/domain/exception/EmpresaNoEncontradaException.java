package com.facturacion.empresas.internal.domain.exception;

public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(Long id) {
        super("No existe la empresa con id " + id);
    }
}
