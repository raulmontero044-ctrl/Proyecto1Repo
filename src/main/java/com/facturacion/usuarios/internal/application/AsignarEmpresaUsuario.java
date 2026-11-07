package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignarEmpresaUsuario {

    private final UsuarioRepository repository;
    private final ComprobadorEmpresa comprobadorEmpresa;

    AsignarEmpresaUsuario(UsuarioRepository repository, ComprobadorEmpresa comprobadorEmpresa) {
        this.repository = repository;
        this.comprobadorEmpresa = comprobadorEmpresa;
    }

    @Transactional
    public UsuarioDto ejecutar(Long id, Long empresaId, String rol) {
        Usuario usuario = repository.buscarPorId(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        AccesoEmpresa acceso = UsuarioDtoConverter.aAcceso(empresaId, rol);
        comprobadorEmpresa.exigirExistente(empresaId);
        return UsuarioDtoConverter.aDto(repository.guardar(usuario.conAcceso(acceso)));
    }
}
