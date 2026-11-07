package com.facturacion.usuarios.internal.domain.exception;

public class AccesoEmpresaNoEncontradoException extends RuntimeException {

    public AccesoEmpresaNoEncontradoException(Long usuarioId, Long empresaId) {
        super("El usuario con id " + usuarioId + " no tiene acceso a la empresa con id " + empresaId);
    }
}
