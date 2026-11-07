package com.facturacion.usuarios.internal.domain.exception;

public class EmpresaInexistenteException extends RuntimeException {

    public EmpresaInexistenteException(Long empresaId) {
        super("No existe la empresa con id " + empresaId);
    }
}
