package com.facturacion.usuarios.internal.domain.repository;

import com.facturacion.usuarios.internal.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(String email);

    List<Usuario> listar();

    List<Usuario> listarPorEmpresa(Long empresaId);

    boolean existePorEmail(String email);

    boolean existePorEmailEnOtroUsuario(String email, Long id);
}
