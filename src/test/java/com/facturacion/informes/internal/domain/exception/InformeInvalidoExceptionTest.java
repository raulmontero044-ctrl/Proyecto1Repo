package com.facturacion.informes.internal.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InformeInvalidoExceptionTest {

    @Test
    void conservaElMensajeYEsNoComprobada() {
        InformeInvalidoException excepcion = new InformeInvalidoException("motivo");

        assertThat(excepcion).hasMessage("motivo").isInstanceOf(RuntimeException.class);
    }
}
