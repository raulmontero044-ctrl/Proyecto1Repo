package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesactivarUsuario {

    private final UsuarioRepository repository;

    public DesactivarUsuario(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id) {
        Usuario desactivado = repository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id))
                .desactivado();
        repository.guardar(desactivado);
    }
}
