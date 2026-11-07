package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class UsuarioFacadeImpl implements UsuarioFacade {

    private final BuscarUsuario buscarUsuario;
    private final BuscarUsuarioPorEmail buscarUsuarioPorEmail;
    private final VerificarCredencialesUsuario verificarCredencialesUsuario;
    private final CambiarPasswordUsuario cambiarPasswordUsuario;

    UsuarioFacadeImpl(
            BuscarUsuario buscarUsuario,
            BuscarUsuarioPorEmail buscarUsuarioPorEmail,
            VerificarCredencialesUsuario verificarCredencialesUsuario,
            CambiarPasswordUsuario cambiarPasswordUsuario) {
        this.buscarUsuario = buscarUsuario;
        this.buscarUsuarioPorEmail = buscarUsuarioPorEmail;
        this.verificarCredencialesUsuario = verificarCredencialesUsuario;
        this.cambiarPasswordUsuario = cambiarPasswordUsuario;
    }

    @Override
    public Optional<UsuarioDto> buscar(Long id) {
        return buscarUsuario.ejecutar(id);
    }

    @Override
    public Optional<UsuarioDto> buscarPorEmail(String email) {
        return buscarUsuarioPorEmail.ejecutar(email);
    }

    @Override
    public Optional<UsuarioDto> verificarCredenciales(String email, String password) {
        return verificarCredencialesUsuario.ejecutar(email, password);
    }

    @Override
    public void cambiarPassword(Long id, String nuevaPassword) {
        cambiarPasswordUsuario.ejecutar(id, nuevaPassword);
    }
}
