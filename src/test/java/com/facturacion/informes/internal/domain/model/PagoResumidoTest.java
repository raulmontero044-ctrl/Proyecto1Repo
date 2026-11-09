package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagoResumidoTest {

    @Test
    void conservaTodosSusCampos() {
        PagoResumido pago = new PagoResumido(4L, TipoPago.PAGO, new BigDecimal("50.10"));

        assertThat(pago.id()).isEqualTo(4L);
        assertThat(pago.tipo()).isEqualTo(TipoPago.PAGO);
        assertThat(pago.importe()).isEqualByComparingTo("50.10");
    }

    @Test
    void rechazaUnPagoSinTipo() {
        assertThatThrownBy(() -> new PagoResumido(1L, null, BigDecimal.ONE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo del pago es obligatorio");
    }

    @Test
    void rechazaUnPagoSinImporte() {
        assertThatThrownBy(() -> new PagoResumido(1L, TipoPago.COBRO, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El importe del pago es obligatorio");
    }
}
