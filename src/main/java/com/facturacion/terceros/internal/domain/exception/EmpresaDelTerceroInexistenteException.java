package com.facturacion.terceros.internal.domain.exception;

public class EmpresaDelTerceroInexistenteException extends RuntimeException {

    public EmpresaDelTerceroInexistenteException(Long empresaId) {
        super("No existe la empresa con id " + empresaId);
    }
}
