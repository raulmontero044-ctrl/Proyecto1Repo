package com.facturacion.pagos.internal.domain.model;

import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoPagoTest {

    @Test
    void unaFacturaEmitidaGeneraUnCobro() {
        assertThat(TipoPago.paraFacturaDeTipo("EMITIDA")).isEqualTo(TipoPago.COBRO);
    }

    @Test
    void unaFacturaRecibidaGeneraUnPago() {
        assertThat(TipoPago.paraFacturaDeTipo("RECIBIDA")).isEqualTo(TipoPago.PAGO);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "emitida", "OTRA"})
    void rechazaUnTipoDeFacturaDesconocido(String tipoFactura) {
        assertThatThrownBy(() -> TipoPago.paraFacturaDeTipo(tipoFactura))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }
}
