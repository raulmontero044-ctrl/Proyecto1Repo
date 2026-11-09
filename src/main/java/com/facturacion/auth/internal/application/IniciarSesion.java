package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.exception.CredencialesInvalidasException;
import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IniciarSesion {

    private final UsuarioFacade usuarioFacade;
    private final EmisorSesion emisorSesion;

    IniciarSesion(UsuarioFacade usuarioFacade, EmisorSesion emisorSesion) {
        this.usuarioFacade = usuarioFacade;
        this.emisorSesion = emisorSesion;
    }

    @Transactional
    public SesionDto ejecutar(String email, String password) {
        UsuarioDto usuario = usuarioFacade.verificarCredenciales(email, password)
                .orElseThrow(CredencialesInvalidasException::new);
        return emisorSesion.emitir(usuario);
    }
}
