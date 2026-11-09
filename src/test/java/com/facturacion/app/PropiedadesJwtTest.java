package com.facturacion.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropiedadesJwtTest {

    @Test
    void rechazaUnSecretoAusente() {
        assertThatThrownBy(() -> new PropiedadesJwt(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET")
                .hasMessageContaining("32 bytes");
    }

    @Test
    void rechazaUnSecretoDeMenosDeTreintaYDosBytes() {
        assertThatThrownBy(() -> new PropiedadesJwt("x".repeat(31))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cuentaLosBytesEnUtf8YNoLosCaracteres() {
        assertThatThrownBy(() -> new PropiedadesJwt("ñ".repeat(15))).isInstanceOf(IllegalStateException.class);
        assertThat(new PropiedadesJwt("ñ".repeat(16)).secreto()).hasSize(16);
    }

    @Test
    void aceptaUnSecretoDeTreintaYDosBytes() {
        assertThat(new PropiedadesJwt("x".repeat(32)).secreto()).isEqualTo("x".repeat(32));
    }

    @Test
    void toStringNoMuestraElSecreto() {
        assertThat(new PropiedadesJwt("secreto-muy-largo-de-mas-de-32-bytes").toString())
                .doesNotContain("secreto-muy-largo")
                .contains("protegido");
    }
}
