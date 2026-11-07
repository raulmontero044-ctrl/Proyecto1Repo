package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarUsuario {

    private final UsuarioRepository repository;

    public BuscarUsuario(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDto> ejecutar(Long id) {
        return repository.buscarPorId(id).map(UsuarioDtoConverter::aDto);
    }
}
