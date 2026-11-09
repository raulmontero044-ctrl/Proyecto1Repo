package com.facturacion.usuarios.internal.persistence.mapper;

import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Rol;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.persistence.entity.AccesoEmpresaEmbeddable;
import com.facturacion.usuarios.internal.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioEntity aEntidad(Usuario usuario) {
        return new UsuarioEntity(
                usuario.id(),
                usuario.email(),
                usuario.nombre(),
                usuario.passwordHash(),
                usuario.activo(),
                usuario.administrador(),
                usuario.accesos().stream()
                        .map(acceso -> new AccesoEmpresaEmbeddable(acceso.empresaId(), acceso.rol().name()))
                        .toList());
    }

    public Usuario aDominio(UsuarioEntity entidad) {
        return new Usuario(
                entidad.getId(),
                entidad.getEmail(),
                entidad.getNombre(),
                entidad.getPasswordHash(),
                entidad.isActivo(),
                entidad.isAdministrador(),
                entidad.getAccesos().stream()
                        .map(acceso -> new AccesoEmpresa(acceso.getEmpresaId(), Rol.valueOf(acceso.getRol())))
                        .toList());
    }
}
