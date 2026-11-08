package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraInformeFacturasTest {

    private static final TipoFactura EMITIDA = TipoFactura.EMITIDA;
    private static final TipoFactura RECIBIDA = TipoFactura.RECIBIDA;

    private static FacturaResumida factura(Long id, TipoFactura tipo, LocalDate fecha, String base) {
        return factura(id, tipo, fecha, "21.00", base, "0.00", base);
    }

    private static FacturaResumida factura(
            Long id, TipoFactura tipo, LocalDate fecha, String porcentaje, String base, String cuota, String total) {
        return new FacturaResumida(
                id, "F-" + id, tipo, fecha, 20L, "Tercero SA", new BigDecimal(porcentaje), new BigDecimal(base),
                new BigDecimal(cuota), new BigDecimal(total));
    }

    private static LocalDate dia(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    @Test
    void elResumenSoloIncluyeLasFacturasDelTipoPedidoYSumaSusImportes() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 10), "21.00", "100.00", "21.00", "121.00"),
                factura(2L, RECIBIDA, dia(1, 11), "21.00", "999.00", "209.79", "1208.79"),
                factura(3L, EMITIDA, dia(1, 12), "10.00", "50.00", "5.00", "55.00"));

        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(facturas, EMITIDA);

        assertThat(resumen.facturas()).extracting(FacturaResumida::id).containsExactly(1L, 3L);
        assertThat(resumen.numeroFacturas()).isEqualTo(2);
        assertThat(resumen.base()).isEqualByComparingTo("150.00");
        assertThat(resumen.cuotaIva()).isEqualByComparingTo("26.00");
        assertThat(resumen.total()).isEqualByComparingTo("176.00");
    }

    @Test
    void elResumenDeRecibidasIgnoraLasEmitidas() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 10), "100.00"),
                factura(2L, RECIBIDA, dia(1, 11), "40.00"));

        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(facturas, RECIBIDA);

        assertThat(resumen.facturas()).extracting(FacturaResumida::id).containsExactly(2L);
        assertThat(resumen.numeroFacturas()).isEqualTo(1);
        assertThat(resumen.base()).isEqualByComparingTo("40.00");
    }

    @Test
    void ordenaLasFacturasPorFechaYDespuesPorId() {
        List<FacturaResumida> facturas = List.of(
                factura(5L, EMITIDA, dia(2, 1), "1.00"),
                factura(9L, EMITIDA, dia(1, 15), "1.00"),
                factura(2L, EMITIDA, dia(2, 1), "1.00"),
                factura(7L, EMITIDA, dia(1, 15), "1.00"));

        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(facturas, EMITIDA);

        assertThat(resumen.facturas()).extracting(FacturaResumida::id).containsExactly(7L, 9L, 2L, 5L);
    }

    @Test
    void sinFacturasDelTipoDevuelveCerosConEscalaDos() {
        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(
                List.of(factura(1L, RECIBIDA, dia(1, 1), "10.00")), EMITIDA);

        assertThat(resumen.facturas()).isEmpty();
        assertThat(resumen.numeroFacturas()).isZero();
        assertThat(resumen.base().toPlainString()).isEqualTo("0.00");
        assertThat(resumen.cuotaIva().toPlainString()).isEqualTo("0.00");
        assertThat(resumen.total().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void lasSumasSeRedondeanADosDecimalesConHalfUp() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "1.115", "0.2345", "1.3495"),
                factura(2L, EMITIDA, dia(1, 2), "21.00", "1.110", "0.0000", "1.0005"));

        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(facturas, EMITIDA);

        assertThat(resumen.base().toPlainString()).isEqualTo("2.23");
        assertThat(resumen.cuotaIva().toPlainString()).isEqualTo("0.23");
        assertThat(resumen.total().toPlainString()).isEqualTo("2.35");
    }

    @Test
    void lasSumasConservanLaEscalaDosAunqueLosImportesTenganMenos() {
        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(
                List.of(factura(1L, EMITIDA, dia(1, 1), "21", "100", "21", "121")), EMITIDA);

        assertThat(resumen.base().toPlainString()).isEqualTo("100.00");
        assertThat(resumen.cuotaIva().toPlainString()).isEqualTo("21.00");
        assertThat(resumen.total().toPlainString()).isEqualTo("121.00");
    }

    @Test
    void laListaDelResumenEsInmutable() {
        ResumenFacturas resumen = CalculadoraInformeFacturas.resumen(
                List.of(factura(1L, EMITIDA, dia(1, 1), "1.00")), EMITIDA);

        assertThatThrownBy(() -> resumen.facturas().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void ingresosSonLasBasesEmitidasGastosLasRecibidasYResultadoSuDiferencia() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "100.00", "21.00", "121.00"),
                factura(2L, EMITIDA, dia(1, 2), "10.00", "50.50", "5.05", "55.55"),
                factura(3L, RECIBIDA, dia(1, 3), "21.00", "30.25", "6.35", "36.60"));

        IngresosGastos resultado = CalculadoraInformeFacturas.ingresosGastos(facturas);

        assertThat(resultado.ingresos().toPlainString()).isEqualTo("150.50");
        assertThat(resultado.gastos().toPlainString()).isEqualTo("30.25");
        assertThat(resultado.resultado().toPlainString()).isEqualTo("120.25");
    }

    @Test
    void elResultadoEsNegativoCuandoLosGastosSuperanALosIngresos() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "10.00"),
                factura(2L, RECIBIDA, dia(1, 2), "25.10"));

        IngresosGastos resultado = CalculadoraInformeFacturas.ingresosGastos(facturas);

        assertThat(resultado.resultado().toPlainString()).isEqualTo("-15.10");
    }

    @Test
    void sinFacturasIngresosGastosYResultadoSonCeroConEscalaDos() {
        IngresosGastos resultado = CalculadoraInformeFacturas.ingresosGastos(List.of());

        assertThat(resultado.ingresos().toPlainString()).isEqualTo("0.00");
        assertThat(resultado.gastos().toPlainString()).isEqualTo("0.00");
        assertThat(resultado.resultado().toPlainString()).isEqualTo("0.00");
    }
}
