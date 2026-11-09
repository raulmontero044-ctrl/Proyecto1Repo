package com.facturacion.auth.internal.application;

import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class UsuariosEnMemoria implements UsuarioFacade {

    private final Map<Long, UsuarioDto> usuarios = new LinkedHashMap<>();
    private final Map<Long, String> passwords = new LinkedHashMap<>();

    void registrar(UsuarioDto usuario, String password) {
        usuarios.put(usuario.id(), usuario);
        passwords.put(usuario.id(), password);
    }

    void eliminar(Long id) {
        usuarios.remove(id);
        passwords.remove(id);
    }

    @Override
    public Optional<UsuarioDto> buscar(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<UsuarioDto> buscarPorEmail(String email) {
        return usuarios.values().stream().filter(usuario -> usuario.email().equals(email)).findFirst();
    }

    @Override
    public Optional<UsuarioDto> verificarCredenciales(String email, String password) {
        return buscarPorEmail(email)
                .filter(UsuarioDto::activo)
                .filter(usuario -> passwords.get(usuario.id()).equals(password));
    }

    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        passwords.put(id, nuevaPassword);
    }
}
