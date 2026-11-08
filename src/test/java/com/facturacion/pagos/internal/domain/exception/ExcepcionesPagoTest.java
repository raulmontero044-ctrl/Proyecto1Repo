package com.facturacion.pagos.internal.domain.exception;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExcepcionesPagoTest {

    @Test
    void pagoNoEncontradoNombraElIdentificador() {
        assertThat(new PagoNoEncontradoException(7L)).hasMessage("No existe el pago con id 7");
    }

    @Test
    void importeExcedidoNombraLaFacturaYElPendiente() {
        assertThat(new ImportePagoExcedidoException(10L, new BigDecimal("0.50")))
                .hasMessage("El importe del pago supera el pendiente de la factura con id 10 (pendiente: 0.50)");
    }

    @Test
    void pagoInvalidoConservaElMensaje() {
        assertThat(new PagoInvalidoException("motivo")).hasMessage("motivo");
    }
}
