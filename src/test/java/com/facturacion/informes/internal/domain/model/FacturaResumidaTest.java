package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaResumidaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);
    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final String IMPORTES_OBLIGATORIOS =
            "La base, la cuota de IVA y el total de la factura son obligatorios";

    private static FacturaResumida factura(
            TipoFactura tipo, LocalDate fecha, BigDecimal porcentaje, BigDecimal base, BigDecimal cuota,
            BigDecimal total) {
        return new FacturaResumida(1L, "F-1", tipo, fecha, 20L, "Tercero SA", porcentaje, base, cuota, total);
    }

    @Test
    void conservaTodosSusCampos() {
        FacturaResumida factura = new FacturaResumida(
                7L, "F-7", TipoFactura.RECIBIDA, FECHA, 20L, "Tercero SA", new BigDecimal("21.00"), CIEN,
                new BigDecimal("21.00"), new BigDecimal("121.00"));

        assertThat(factura.id()).isEqualTo(7L);
        assertThat(factura.numero()).isEqualTo("F-7");
        assertThat(factura.tipo()).isEqualTo(TipoFactura.RECIBIDA);
        assertThat(factura.fecha()).isEqualTo(FECHA);
        assertThat(factura.terceroId()).isEqualTo(20L);
        assertThat(factura.terceroNombre()).isEqualTo("Tercero SA");
        assertThat(factura.porcentajeIva()).isEqualByComparingTo("21");
        assertThat(factura.base()).isEqualByComparingTo("100");
        assertThat(factura.cuotaIva()).isEqualByComparingTo("21");
        assertThat(factura.total()).isEqualByComparingTo("121");
    }

    @Test
    void rechazaUnaFacturaSinTipo() {
        assertThatThrownBy(() -> factura(null, FECHA, CIEN, CIEN, CIEN, CIEN))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
    }

    @Test
    void rechazaUnaFacturaSinFecha() {
        assertThatThrownBy(() -> factura(TipoFactura.EMITIDA, null, CIEN, CIEN, CIEN, CIEN))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha de la factura es obligatoria");
    }

    @Test
    void rechazaUnaFacturaSinPorcentajeDeIva() {
        assertThatThrownBy(() -> factura(TipoFactura.EMITIDA, FECHA, null, CIEN, CIEN, CIEN))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El porcentaje de IVA de la factura es obligatorio");
    }

    @Test
    void rechazaUnaFacturaSinAlgunoDeSusImportes() {
        assertThatThrownBy(() -> factura(TipoFactura.EMITIDA, FECHA, CIEN, null, CIEN, CIEN))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
        assertThatThrownBy(() -> factura(TipoFactura.EMITIDA, FECHA, CIEN, CIEN, null, CIEN))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
        assertThatThrownBy(() -> factura(TipoFactura.EMITIDA, FECHA, CIEN, CIEN, CIEN, null))
                .isInstanceOf(InformeInvalidoException.class).hasMessage(IMPORTES_OBLIGATORIOS);
    }
}
