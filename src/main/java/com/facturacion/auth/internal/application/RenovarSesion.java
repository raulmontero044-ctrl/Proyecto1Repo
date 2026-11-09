package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import com.facturacion.auth.internal.domain.service.HashRefreshToken;
import com.facturacion.usuarios.api.facade.UsuarioFacade;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class RenovarSesion {

    static final String REFRESH_TOKEN_NO_VALIDO = "El refresh token no es válido";

    private final RefreshTokenRepository repository;
    private final UsuarioFacade usuarioFacade;
    private final EmisorSesion emisorSesion;
    private final Clock reloj;

    RenovarSesion(
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
    public SesionDto ejecutar(String refreshToken) {
        RefreshToken usado = buscarVigente(refreshToken);
        UsuarioDto usuario = usuarioFacade.buscar(usado.usuarioId())
                .filter(UsuarioDto::activo)
                .orElseThrow(RenovarSesion::noValido);
        repository.guardar(usado.revocar());
        return emisorSesion.emitir(usuario);
    }

    private RefreshToken buscarVigente(String refreshToken) {
        if (refreshToken == null) {
            throw noValido();
        }
        return repository.buscarPorHash(HashRefreshToken.calcular(refreshToken))
                .filter(token -> token.vigenteEn(reloj.instant()))
                .orElseThrow(RenovarSesion::noValido);
    }

    private static RefreshTokenInvalidoException noValido() {
        return new RefreshTokenInvalidoException(REFRESH_TOKEN_NO_VALIDO);
    }
}
