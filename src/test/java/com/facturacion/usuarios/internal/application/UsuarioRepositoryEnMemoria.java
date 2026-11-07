package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Usuario guardar(Usuario usuario) {
        Long id = usuario.id() != null ? usuario.id() : siguienteId++;
        Usuario guardado = new Usuario(
                id, usuario.email(), usuario.nombre(), usuario.passwordHash(), usuario.activo(), usuario.accesos());
        usuarios.put(id, guardado);
        return guardado;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarios.values().stream().filter(usuario -> usuario.email().equals(email)).findFirst();
    }

    @Override
    public List<Usuario> listar() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return usuarios.values().stream()
                .filter(usuario -> usuario.accesos().stream().anyMatch(acceso -> acceso.empresaId().equals(empresaId)))
                .toList();
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarios.values().stream().anyMatch(usuario -> usuario.email().equals(email));
    }

    @Override
    public boolean existePorEmailEnOtroUsuario(String email, Long id) {
        return usuarios.values().stream()
                .anyMatch(usuario -> usuario.email().equals(email) && !usuario.id().equals(id));
    }
}
