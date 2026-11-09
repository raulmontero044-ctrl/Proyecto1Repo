package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;

import java.util.List;

final class UsuarioDtoConverter {

    private UsuarioDtoConverter() {
    }

    static List<AccesoEmpresa> aAccesos(List<AccesoEmpresaDto> accesos) {
        if (accesos == null) {
            return List.of();
        }
        return accesos.stream().map(UsuarioDtoConverter::aAcceso).toList();
    }

    static AccesoEmpresa aAcceso(Long empresaId, String rol) {
        return new AccesoEmpresa(empresaId, aRol(rol));
    }

    static UsuarioDto aDto(Usuario usuario) {
        return new UsuarioDto(
                usuario.id(),
                usuario.email(),
                usuario.nombre(),
                usuario.activo(),
                usuario.administrador(),
                usuario.accesos().stream()
                        .map(acceso -> new AccesoEmpresaDto(acceso.empresaId(), acceso.rol().name()))
                        .toList());
    }

    private static AccesoEmpresa aAcceso(AccesoEmpresaDto acceso) {
        if (acceso == null) {
            throw new UsuarioInvalidoException("Los accesos del usuario no pueden ser nulos");
        }
        return aAcceso(acceso.empresaId(), acceso.rol());
    }

    private static Rol aRol(String rol) {
        if (rol == null) {
            throw new UsuarioInvalidoException("El rol del acceso es obligatorio");
        }
        try {
            return Rol.valueOf(rol);
        } catch (IllegalArgumentException excepcion) {
            throw new UsuarioInvalidoException("El rol del acceso no es válido");
        }
    }
}
