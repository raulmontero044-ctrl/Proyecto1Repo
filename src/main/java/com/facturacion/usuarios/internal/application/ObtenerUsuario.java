package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerUsuario {

    private final UsuarioRepository repository;

    public ObtenerUsuario(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UsuarioDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(UsuarioDtoConverter::aDto)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }
}
