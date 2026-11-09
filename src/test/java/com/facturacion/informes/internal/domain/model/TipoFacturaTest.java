package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoFacturaTest {

    @Test
    void seObtieneDesdeSuNombre() {
        assertThat(TipoFactura.desde("EMITIDA")).isEqualTo(TipoFactura.EMITIDA);
        assertThat(TipoFactura.desde("RECIBIDA")).isEqualTo(TipoFactura.RECIBIDA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "emitida", "OTRA"})
    void rechazaUnTipoDesconocido(String valor) {
        assertThatThrownBy(() -> TipoFactura.desde(valor))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }

    @Test
    void rechazaUnTipoAusente() {
        assertThatThrownBy(() -> TipoFactura.desde(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
    }
}
