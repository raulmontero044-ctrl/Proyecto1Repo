package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CambiarPasswordUsuario {

    private final UsuarioRepository repository;
    private final CifradorPassword cifrador;

    public CambiarPasswordUsuario(UsuarioRepository repository, CifradorPassword cifrador) {
        this.repository = repository;
        this.cifrador = cifrador;
    }

    @Transactional
    public void ejecutar(Long id, String nuevaPassword) {
        Usuario usuario = repository.buscarPorId(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
        PasswordEnClaro password = new PasswordEnClaro(nuevaPassword);
        repository.guardar(usuario.conPasswordHash(cifrador.cifrar(password)));
    }
}
