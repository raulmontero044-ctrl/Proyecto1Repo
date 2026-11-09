package com.facturacion.auth.internal.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TokenAccesoTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T10:00:00Z");

    @Test
    void emitirCaducaACincoMinutos() {
        TokenAcceso token = TokenAcceso.emitir(7L, Rol.GESTOR, AHORA);

        assertThat(token).isEqualTo(
                new TokenAcceso(7L, Rol.GESTOR, AHORA, Instant.parse("2026-11-09T10:05:00Z")));
        assertThat(TokenAcceso.DURACION).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void elRolEfectivoEsElRolDelToken() {
        assertThat(TokenAcceso.emitir(7L, Rol.CONSULTA, AHORA).rolEfectivo()).contains(Rol.CONSULTA);
    }

    @Test
    void unTokenSinRolNoTieneRolEfectivo() {
        assertThat(TokenAcceso.emitir(7L, null, AHORA).rolEfectivo()).isEmpty();
    }
}
