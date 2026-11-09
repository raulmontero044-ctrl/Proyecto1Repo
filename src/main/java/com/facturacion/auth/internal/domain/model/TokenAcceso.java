package com.facturacion.auth.internal.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public record TokenAcceso(Long usuarioId, Rol rol, Instant emitidoEn, Instant expiraEn) {

    public static final Duration DURACION = Duration.ofMinutes(5);

    public static TokenAcceso emitir(Long usuarioId, Rol rol, Instant ahora) {
        return new TokenAcceso(usuarioId, rol, ahora, ahora.plus(DURACION));
    }

    public Optional<Rol> rolEfectivo() {
        return Optional.ofNullable(rol);
    }
}
