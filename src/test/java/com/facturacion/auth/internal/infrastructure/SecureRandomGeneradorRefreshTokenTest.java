package com.facturacion.auth.internal.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomGeneradorRefreshTokenTest {

    private final SecureRandomGeneradorRefreshToken generador = new SecureRandomGeneradorRefreshToken();

    @Test
    void generaTreintaYDosBytesAleatoriosEnBase64UrlSinRelleno() {
        String valor = generador.generar();

        assertThat(valor).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(Base64.getUrlDecoder().decode(valor)).hasSize(32);
    }

    @Test
    void noRepiteValores() {
        Set<String> generados = new HashSet<>();
        for (int indice = 0; indice < 1000; indice++) {
            generados.add(generador.generar());
        }

        assertThat(generados).hasSize(1000);
    }
}
