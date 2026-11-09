package com.facturacion.auth.internal.application;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.domain.model.Rol;
import com.facturacion.auth.internal.domain.model.TokenAcceso;
import com.facturacion.auth.internal.domain.repository.RefreshTokenRepository;
import com.facturacion.auth.internal.domain.service.CalculadoraRolEfectivo;
import com.facturacion.auth.internal.domain.service.FirmadorTokenAcceso;
import com.facturacion.auth.internal.domain.service.GeneradorRefreshToken;
import com.facturacion.auth.internal.domain.service.HashRefreshToken;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
class EmisorSesion {

    private final RefreshTokenRepository repository;
    private final FirmadorTokenAcceso firmador;
    private final GeneradorRefreshToken generador;
    private final Clock reloj;

    EmisorSesion(
            RefreshTokenRepository repository,
            FirmadorTokenAcceso firmador,
            GeneradorRefreshToken generador,
            Clock reloj) {
        this.repository = repository;
        this.firmador = firmador;
        this.generador = generador;
        this.reloj = reloj;
    }

    SesionDto emitir(UsuarioDto usuario) {
        Instant ahora = reloj.instant();
        TokenAcceso tokenAcceso = TokenAcceso.emitir(usuario.id(), rolEfectivo(usuario), ahora);
        String refreshToken = generador.generar();
        repository.guardar(RefreshToken.emitir(usuario.id(), HashRefreshToken.calcular(refreshToken), ahora));
        return new SesionDto(firmador.firmar(tokenAcceso), refreshToken, tokenAcceso.expiraEn());
    }

    private static Rol rolEfectivo(UsuarioDto usuario) {
        List<Rol> roles = usuario.accesos().stream().map(acceso -> Rol.valueOf(acceso.rol())).toList();
        return CalculadoraRolEfectivo.calcular(usuario.administrador(), roles).orElse(null);
    }
}
