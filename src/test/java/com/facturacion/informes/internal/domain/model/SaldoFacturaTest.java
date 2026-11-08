package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaldoFacturaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);
    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final String IMPORTES_OBLIGATORIOS =
            "El total, lo pagado y lo pendiente de la factura son obligatorios";

    private static SaldoFactura saldo(
            TipoFactura tipo, LocalDate fecha, BigDecimal total, BigDecimal pagado, BigDecimal pendiente,
            EstadoCobro estado) {
        return new SaldoFactura(1L, "F-1", tipo, fecha, null, total, pagado, pendiente, estado, false);
    }

    @Test
    void conservaTodosSusCamposYAdmiteFacturasSinVencimiento() {
        SaldoFactura saldo = new SaldoFactura(
                9L, "F-9", TipoFactura.EMITIDA, FECHA, null, CIEN, new BigDecimal("40.00"),
                new BigDecimal("60.00"), EstadoCobro.PARCIAL, true);

        assertThat(saldo.facturaId()).isEqualTo(9L);
        assertThat(saldo.numero()).isEqualTo("F-9");
        assertThat(saldo.tipo()).isEqualTo(TipoFactura.EMITIDA);
        assertThat(saldo.fecha()).isEqualTo(FECHA);
        assertThat(saldo.fechaVencimiento()).isNull();
        assertThat(saldo.total()).isEqualByComparingTo("100");
        assertThat(saldo.pagado()).isEqualByComparingTo("40");
        assertThat(saldo.pendiente()).isEqualByComparingTo("60");
        assertThat(saldo.estado()).isEqualTo(EstadoCobro.PARCIAL);
        assertThat(saldo.vencida()).isTrue();
    }

    @Test
    void rechazaUnSaldoSinTipo() {
        assertThatThrownBy(() -> saldo(null, FECHA, CIEN, CIEN, CIEN, EstadoCobro.PENDIENTE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
    }

    @Test
    void rechazaUnSaldoSinEstado() {
        assertThatThrownBy(() -> saldo(TipoFactura.EMITIDA, FECHA, CIEN, CIEN, CIEN, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El estado de la factura es obligatorio");
    }

    @Test
    void rechazaUnSaldoSinFecha() {
        assertThatThrownBy(() -> saldo(TipoFactura.EMITIDA, null, CIEN, CIEN, CIEN, EstadoCobro.PENDIENTE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha de la factura es obligatoria");
    }

    @Test
    void rechazaUnSaldoSinAlgunoDeSusImportes() {
        assertThatThrownBy(() -> saldo(TipoFactura.EMITIDA, FECHA, null, CIEN, CIEN, EstadoCobro.PENDIENTE))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
        assertThatThrownBy(() -> saldo(TipoFactura.EMITIDA, FECHA, CIEN, null, CIEN, EstadoCobro.PENDIENTE))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
        assertThatThrownBy(() -> saldo(TipoFactura.EMITIDA, FECHA, CIEN, CIEN, null, EstadoCobro.PENDIENTE))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
    }
}
