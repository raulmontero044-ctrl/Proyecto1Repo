package com.facturacion.usuarios.internal.domain.model;

import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordEnClaroTest {

    @ParameterizedTest
    @ValueSource(ints = {8, 30, 72})
    void aceptaLongitudesEntre8Y72(int longitud) {
        PasswordEnClaro password = new PasswordEnClaro("a".repeat(longitud));

        assertThat(password.valor()).hasSize(longitud);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, 73})
    void rechazaLongitudesFueraDelRango(int longitud) {
        assertThatThrownBy(() -> new PasswordEnClaro("a".repeat(longitud)))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La contraseña debe tener entre 8 y 72 caracteres");
    }

    @Test
    void rechazaPasswordNula() {
        assertThatThrownBy(() -> new PasswordEnClaro(null))
                .isInstanceOf(UsuarioInvalidoException.class)
                .hasMessage("La contraseña debe tener entre 8 y 72 caracteres");
    }

    @Test
    void cuentaCaracteresYNoUnidadesUtf16() {
        String ochoEmojis = "😀".repeat(8);
        String setentaYDosEmojis = "😀".repeat(72);

        assertThat(new PasswordEnClaro(ochoEmojis).valor()).isEqualTo(ochoEmojis);
        assertThat(new PasswordEnClaro(setentaYDosEmojis).valor()).isEqualTo(setentaYDosEmojis);
        assertThatThrownBy(() -> new PasswordEnClaro("😀".repeat(73)))
                .isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void toStringNoMuestraLaContrasena() {
        assertThat(new PasswordEnClaro("secreta123")).hasToString("PasswordEnClaro[protegida]");
    }
}
