package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EstadoCobroTest {

    @Test
    void seObtieneDesdeSuNombre() {
        assertThat(EstadoCobro.desde("PENDIENTE")).isEqualTo(EstadoCobro.PENDIENTE);
        assertThat(EstadoCobro.desde("PARCIAL")).isEqualTo(EstadoCobro.PARCIAL);
        assertThat(EstadoCobro.desde("PAGADA")).isEqualTo(EstadoCobro.PAGADA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "pagada", "VENCIDA"})
    void rechazaUnEstadoDesconocido(String valor) {
        assertThatThrownBy(() -> EstadoCobro.desde(valor))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El estado de la factura debe ser PENDIENTE, PARCIAL o PAGADA");
    }

    @Test
    void rechazaUnEstadoAusente() {
        assertThatThrownBy(() -> EstadoCobro.desde(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El estado de la factura es obligatorio");
    }

    @Test
    void soloLaFacturaPagadaNoTienePendiente() {
        assertThat(EstadoCobro.PENDIENTE.tienePendiente()).isTrue();
        assertThat(EstadoCobro.PARCIAL.tienePendiente()).isTrue();
        assertThat(EstadoCobro.PAGADA.tienePendiente()).isFalse();
    }
}
