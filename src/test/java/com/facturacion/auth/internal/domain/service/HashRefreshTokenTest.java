package com.facturacion.auth.internal.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashRefreshTokenTest {

    @Test
    void calculaElSha256EnHexadecimalEnMinusculas() {
        assertThat(HashRefreshToken.calcular("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void elHashDeLaCadenaVaciaEsElDeSha256() {
        assertThat(HashRefreshToken.calcular(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void valoresDistintosProducenHashesDistintosYElMismoValorElMismoHash() {
        assertThat(HashRefreshToken.calcular("token-1")).isNotEqualTo(HashRefreshToken.calcular("token-2"));
        assertThat(HashRefreshToken.calcular("token-1")).isEqualTo(HashRefreshToken.calcular("token-1"));
    }

    @Test
    void elResultadoSiempreTieneSesentaYCuatroCifrasHexadecimales() {
        assertThat(HashRefreshToken.calcular("x".repeat(1000))).matches("[0-9a-f]{64}");
    }
}
