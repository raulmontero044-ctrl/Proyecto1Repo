package com.facturacion.auth.internal.domain.model;

import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenTest {

    private static final String HASH = "a".repeat(64);
    private static final Instant AHORA = Instant.parse("2026-11-09T10:00:00Z");

    @Test
    void emitirCreaUnTokenSinIdNoRevocadoQueCaducaADiezDias() {
        RefreshToken token = RefreshToken.emitir(7L, HASH, AHORA);

        assertThat(token).isEqualTo(new RefreshToken(null, 7L, HASH, Instant.parse("2026-11-19T10:00:00Z"), false));
    }

    @Test
    void laDuracionEsDeDiezDias() {
        assertThat(RefreshToken.DURACION).isEqualTo(Duration.ofDays(10));
    }

    @Test
    void emitirSinInstanteLanzaExcepcion() {
        assertThatThrownBy(() -> RefreshToken.emitir(7L, HASH, null))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("El instante de emisión del refresh token es obligatorio");
    }

    @Test
    void rechazaUsuarioNulo() {
        assertThatThrownBy(() -> new RefreshToken(1L, null, HASH, AHORA, false))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("El usuario del refresh token es obligatorio");
    }

    private static Stream<String> hashesNoValidos() {
        return Stream.of(null, "", "a".repeat(63), "a".repeat(65), "A".repeat(64), "g".repeat(64), "valor-en-claro");
    }

    @ParameterizedTest
    @MethodSource("hashesNoValidos")
    void rechazaUnHashQueNoEsSha256Hexadecimal(String hash) {
        assertThatThrownBy(() -> new RefreshToken(1L, 7L, hash, AHORA, false))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("El hash del refresh token debe ser un SHA-256 hexadecimal");
    }

    @Test
    void aceptaUnHashDeSesentaYCuatroCifrasHexadecimales() {
        RefreshToken token = new RefreshToken(1L, 7L, "0123456789abcdef".repeat(4), AHORA, false);

        assertThat(token.hash()).hasSize(64);
    }

    @Test
    void rechazaCaducidadNula() {
        assertThatThrownBy(() -> new RefreshToken(1L, 7L, HASH, null, false))
                .isInstanceOf(RefreshTokenInvalidoException.class)
                .hasMessage("La caducidad del refresh token es obligatoria");
    }

    @Test
    void esVigenteHastaElInstanteAnteriorASuCaducidad() {
        RefreshToken token = new RefreshToken(1L, 7L, HASH, AHORA, false);

        assertThat(token.vigenteEn(AHORA.minusNanos(1))).isTrue();
        assertThat(token.vigenteEn(AHORA)).isFalse();
        assertThat(token.vigenteEn(AHORA.plusSeconds(1))).isFalse();
    }

    @Test
    void unTokenRevocadoNoEsVigenteAunqueNoHayaCaducado() {
        RefreshToken token = new RefreshToken(1L, 7L, HASH, AHORA, true);

        assertThat(token.vigenteEn(AHORA.minusSeconds(60))).isFalse();
    }

    @Test
    void revocarSoloMarcaElTokenComoRevocado() {
        RefreshToken token = new RefreshToken(1L, 7L, HASH, AHORA, false);

        assertThat(token.revocar()).isEqualTo(new RefreshToken(1L, 7L, HASH, AHORA, true));
        assertThat(token.revocado()).isFalse();
    }
}
