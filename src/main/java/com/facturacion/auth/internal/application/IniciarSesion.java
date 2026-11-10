package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.exception.CredencialesInvalidasException;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class IniciarSesion {

    private final RefreshTokenRepository repository;
    private final UsuarioFacade usuarioFacade;
    private final EmisorSesion emisorSesion;
    private final Clock reloj;

    IniciarSesion(
            RefreshTokenRepository repository,
            UsuarioFacade usuarioFacade,
            EmisorSesion emisorSesion,
            Clock reloj) {
        this.repository = repository;
        this.usuarioFacade = usuarioFacade;
        this.emisorSesion = emisorSesion;
        this.reloj = reloj;
    }

    @Transactional
    public SesionDto ejecutar(String email, String password) {
        UsuarioDto usuario = usuarioFacade.verificarCredenciales(email, password)
                .orElseThrow(CredencialesInvalidasException::new);
        repository.buscarVigentesPorUsuario(usuario.id(), reloj.instant())
                .forEach(vigente -> repository.guardar(vigente.revocar()));
        return emisorSesion.emitir(usuario);
    }
}
