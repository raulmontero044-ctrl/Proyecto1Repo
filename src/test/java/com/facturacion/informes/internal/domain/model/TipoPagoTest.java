package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoPagoTest {

    @Test
    void seObtieneDesdeSuNombre() {
        assertThat(TipoPago.desde("COBRO")).isEqualTo(TipoPago.COBRO);
        assertThat(TipoPago.desde("PAGO")).isEqualTo(TipoPago.PAGO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "cobro", "DEVOLUCION"})
    void rechazaUnTipoDesconocido(String valor) {
        assertThatThrownBy(() -> TipoPago.desde(valor))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo del pago debe ser COBRO o PAGO");
    }

    @Test
    void rechazaUnTipoAusente() {
        assertThatThrownBy(() -> TipoPago.desde(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo del pago es obligatorio");
    }
}
