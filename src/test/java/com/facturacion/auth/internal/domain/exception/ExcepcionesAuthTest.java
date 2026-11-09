package com.facturacion.auth.internal.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExcepcionesAuthTest {

    @Test
    void credencialesInvalidasNoRevelaSiFallaElEmailOLaContrasena() {
        CredencialesInvalidasException excepcion = new CredencialesInvalidasException();

        assertThat(excepcion).isInstanceOf(RuntimeException.class)
                .hasMessage("El email o la contraseña no son correctos");
    }

    @Test
    void refreshTokenInvalidoConservaSuMensaje() {
        RefreshTokenInvalidoException excepcion = new RefreshTokenInvalidoException("El refresh token no es válido");

        assertThat(excepcion).isInstanceOf(RuntimeException.class).hasMessage("El refresh token no es válido");
    }
}
