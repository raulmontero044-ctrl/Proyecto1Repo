package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;

public record AccesoEmpresa(Long empresaId, Rol rol) {

    public AccesoEmpresa {
        if (empresaId == null) {
            throw new UsuarioInvalidoException("La empresa del acceso es obligatoria");
        }
        if (rol == null) {
            throw new UsuarioInvalidoException("El rol del acceso es obligatorio");
        }
    }
}
