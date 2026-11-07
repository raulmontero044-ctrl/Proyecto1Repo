package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VerificarCredencialesUsuario {

    private final UsuarioRepository repository;
    private final CifradorPassword cifrador;

    public VerificarCredencialesUsuario(UsuarioRepository repository, CifradorPassword cifrador) {
        this.repository = repository;
        this.cifrador = cifrador;
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDto> ejecutar(String email, String password) {
        if (email == null) {
            return Optional.empty();
        }
        return repository.buscarPorEmail(Usuario.normalizarEmail(email))
                .filter(Usuario::activo)
                .filter(usuario -> cifrador.coincide(password, usuario.passwordHash()))
                .map(UsuarioDtoConverter::aDto);
    }
}
