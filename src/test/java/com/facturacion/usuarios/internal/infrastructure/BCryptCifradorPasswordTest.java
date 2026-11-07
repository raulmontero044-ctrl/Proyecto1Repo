package com.facturacion.usuarios.internal.infrastructure;

import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptCifradorPasswordTest {

    private final BCryptCifradorPassword cifrador = new BCryptCifradorPassword();

    @Test
    void cifraConBCryptSinGuardarLaContrasenaEnClaro() {
        String hash = cifrador.cifrar(new PasswordEnClaro("secreta123"));

        assertThat(hash).startsWith("$2a$10$").hasSize(60).doesNotContain("secreta123");
    }

    @Test
    void cadaCifradoUsaUnaSalDistinta() {
        PasswordEnClaro password = new PasswordEnClaro("secreta123");

        assertThat(cifrador.cifrar(password)).isNotEqualTo(cifrador.cifrar(password));
    }

    @Test
    void coincideConLaContrasenaCorrecta() {
        String hash = cifrador.cifrar(new PasswordEnClaro("secreta123"));

        assertThat(cifrador.coincide("secreta123", hash)).isTrue();
    }

    @Test
    void noCoincideConOtraContrasena() {
        String hash = cifrador.cifrar(new PasswordEnClaro("secreta123"));

        assertThat(cifrador.coincide("secreta124", hash)).isFalse();
        assertThat(cifrador.coincide("SECRETA123", hash)).isFalse();
    }

    @Test
    void noCoincideConValoresNulos() {
        String hash = cifrador.cifrar(new PasswordEnClaro("secreta123"));

        assertThat(cifrador.coincide(null, hash)).isFalse();
        assertThat(cifrador.coincide("secreta123", null)).isFalse();
    }

    @Test
    void cifraLaLongitudMaximaDe72Caracteres() {
        String setentaYDos = "a".repeat(72);

        String hash = cifrador.cifrar(new PasswordEnClaro(setentaYDos));

        assertThat(cifrador.coincide(setentaYDos, hash)).isTrue();
    }
}
