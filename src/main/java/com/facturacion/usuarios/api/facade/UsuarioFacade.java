package com.facturacion.usuarios.api.facade;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;

import java.util.Optional;

public interface UsuarioFacade {

    Optional<UsuarioDto> buscar(Long id);

    Optional<UsuarioDto> buscarPorEmail(String email);

    Optional<UsuarioDto> verificarCredenciales(String email, String password);

    void cambiarPassword(Long id, String nuevaPassword);
}
