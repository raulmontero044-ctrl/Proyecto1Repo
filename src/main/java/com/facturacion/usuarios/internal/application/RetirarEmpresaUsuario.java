package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetirarEmpresaUsuario {

    private final UsuarioRepository repository;

    public RetirarEmpresaUsuario(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id, Long empresaId) {
        Usuario modificado = repository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id))
                .sinAccesoA(empresaId);
        repository.guardar(modificado);
    }
}
