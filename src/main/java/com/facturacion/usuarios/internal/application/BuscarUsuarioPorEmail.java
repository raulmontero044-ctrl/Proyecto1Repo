package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarUsuarioPorEmail {

    private final UsuarioRepository repository;

    public BuscarUsuarioPorEmail(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDto> ejecutar(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return repository.buscarPorEmail(Usuario.normalizarEmail(email)).map(UsuarioDtoConverter::aDto);
    }
}
