package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.Agrupacion;
import com.facturacion.informes.internal.domain.model.EstadisticaPeriodo;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.ResumenImpuestos;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TramoIva;
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
    void agrupaElIvaRepercutidoYElSoportadoPorPorcentajeOrdenadoDeMenorAMayor() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "100.00", "21.00", "121.00"),
                factura(2L, EMITIDA, dia(1, 2), "10.00", "50.00", "5.00", "55.00"),
                factura(3L, EMITIDA, dia(1, 3), "21", "200.00", "42.00", "242.00"),
                factura(4L, RECIBIDA, dia(1, 4), "4.00", "25.00", "1.00", "26.00"),
                factura(5L, RECIBIDA, dia(1, 5), "21.00", "10.00", "2.10", "12.10"));

        ResumenImpuestos impuestos = CalculadoraInformeFacturas.impuestos(facturas);

        assertThat(impuestos.repercutido()).containsExactly(
                new TramoIva(new BigDecimal("10.00"), new BigDecimal("50.00"), new BigDecimal("5.00")),
                new TramoIva(new BigDecimal("21.00"), new BigDecimal("300.00"), new BigDecimal("63.00")));
        assertThat(impuestos.soportado()).containsExactly(
                new TramoIva(new BigDecimal("4.00"), new BigDecimal("25.00"), new BigDecimal("1.00")),
                new TramoIva(new BigDecimal("21.00"), new BigDecimal("10.00"), new BigDecimal("2.10")));
        assertThat(impuestos.cuotaRepercutida().toPlainString()).isEqualTo("68.00");
        assertThat(impuestos.cuotaSoportada().toPlainString()).isEqualTo("3.10");
        assertThat(impuestos.resultado().toPlainString()).isEqualTo("64.90");
    }

    @Test
    void elResultadoDeImpuestosEsNegativoSiElSoportadoSuperaAlRepercutido() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "10.00", "2.10", "12.10"),
                factura(2L, RECIBIDA, dia(1, 2), "21.00", "100.00", "21.00", "121.00"));

        ResumenImpuestos impuestos = CalculadoraInformeFacturas.impuestos(facturas);

        assertThat(impuestos.resultado().toPlainString()).isEqualTo("-18.90");
    }

    @Test
    void elPorcentajeCeroFormaSuPropioTramo() {
        ResumenImpuestos impuestos = CalculadoraInformeFacturas.impuestos(
                List.of(factura(1L, EMITIDA, dia(1, 1), "0", "80.00", "0.00", "80.00")));

        assertThat(impuestos.repercutido()).singleElement().satisfies(tramo -> {
            assertThat(tramo.porcentajeIva().toPlainString()).isEqualTo("0.00");
            assertThat(tramo.base().toPlainString()).isEqualTo("80.00");
            assertThat(tramo.cuota().toPlainString()).isEqualTo("0.00");
        });
    }

    @Test
    void sinFacturasLosImpuestosNoTienenTramosYSonCeroConEscalaDos() {
        ResumenImpuestos impuestos = CalculadoraInformeFacturas.impuestos(List.of());

        assertThat(impuestos.repercutido()).isEmpty();
        assertThat(impuestos.soportado()).isEmpty();
        assertThat(impuestos.cuotaRepercutida().toPlainString()).isEqualTo("0.00");
        assertThat(impuestos.cuotaSoportada().toPlainString()).isEqualTo("0.00");
        assertThat(impuestos.resultado().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void losTramosRedondeanSusSumasConHalfUp() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "1.115", "0.105", "1.22"),
                factura(2L, EMITIDA, dia(1, 2), "21.00", "1.110", "0.120", "1.23"));

        ResumenImpuestos impuestos = CalculadoraInformeFacturas.impuestos(facturas);

        assertThat(impuestos.repercutido()).singleElement().satisfies(tramo -> {
            assertThat(tramo.base().toPlainString()).isEqualTo("2.23");
            assertThat(tramo.cuota().toPlainString()).isEqualTo("0.23");
        });
    }

    @Test
    void lasEstadisticasMensualesCuentanYSumanEmitidasYRecibidasPorMes() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(3, 31), "21.00", "100.00", "21.00", "121.00"),
                factura(2L, EMITIDA, dia(3, 1), "21.00", "10.00", "2.10", "12.10"),
                factura(3L, RECIBIDA, dia(3, 15), "21.00", "50.00", "10.50", "60.50"),
                factura(4L, RECIBIDA, dia(1, 10), "21.00", "20.00", "4.20", "24.20"));

        List<EstadisticaPeriodo> estadisticas = CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.MES);

        assertThat(estadisticas).containsExactly(
                new EstadisticaPeriodo("2026-01", 0, new BigDecimal("0.00"), 1, new BigDecimal("24.20")),
                new EstadisticaPeriodo("2026-03", 2, new BigDecimal("133.10"), 1, new BigDecimal("60.50")));
    }

    @Test
    void lasEstadisticasNoIncluyenPeriodosSinFacturas() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 5), "10.00"),
                factura(2L, EMITIDA, dia(6, 5), "10.00"));

        List<EstadisticaPeriodo> estadisticas = CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.MES);

        assertThat(estadisticas).extracting(EstadisticaPeriodo::periodo).containsExactly("2026-01", "2026-06");
    }

    @Test
    void lasEstadisticasTrimestralesAgrupanCadaTresMeses() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "1.00"),
                factura(2L, EMITIDA, dia(3, 31), "2.00"),
                factura(3L, RECIBIDA, dia(4, 1), "4.00"),
                factura(4L, EMITIDA, dia(12, 31), "8.00"));

        List<EstadisticaPeriodo> estadisticas =
                CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.TRIMESTRE);

        assertThat(estadisticas).containsExactly(
                new EstadisticaPeriodo("2026-T1", 2, new BigDecimal("3.00"), 0, new BigDecimal("0.00")),
                new EstadisticaPeriodo("2026-T2", 0, new BigDecimal("0.00"), 1, new BigDecimal("4.00")),
                new EstadisticaPeriodo("2026-T4", 1, new BigDecimal("8.00"), 0, new BigDecimal("0.00")));
    }

    @Test
    void lasEstadisticasDeUnPeriodoQueCruzaElAnioSeOrdenanCronologicamente() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, LocalDate.of(2027, 1, 2), "1.00"),
                factura(2L, EMITIDA, LocalDate.of(2026, 12, 30), "2.00"),
                factura(3L, RECIBIDA, LocalDate.of(2026, 11, 30), "4.00"));

        assertThat(CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.MES))
                .extracting(EstadisticaPeriodo::periodo).containsExactly("2026-11", "2026-12", "2027-01");
        assertThat(CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.TRIMESTRE))
                .extracting(EstadisticaPeriodo::periodo).containsExactly("2026-T4", "2027-T1");
        assertThat(CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.ANIO)).containsExactly(
                new EstadisticaPeriodo("2026", 1, new BigDecimal("2.00"), 1, new BigDecimal("4.00")),
                new EstadisticaPeriodo("2027", 1, new BigDecimal("1.00"), 0, new BigDecimal("0.00")));
    }

    @Test
    void lasEstadisticasUsanElTotalYNoLaBase() {
        List<FacturaResumida> facturas = List.of(
                factura(1L, EMITIDA, dia(1, 1), "21.00", "100.00", "21.00", "121.00"));

        assertThat(CalculadoraInformeFacturas.estadisticas(facturas, Agrupacion.ANIO))
                .singleElement()
                .satisfies(estadistica -> assertThat(estadistica.totalEmitidas().toPlainString())
                        .isEqualTo("121.00"));
    }

    @Test
    void sinFacturasNoHayEstadisticas() {
        assertThat(CalculadoraInformeFacturas.estadisticas(List.of(), Agrupacion.ANIO)).isEmpty();
    }

    @Test
    void sinFacturasIngresosGastosYResultadoSonCeroConEscalaDos() {
        IngresosGastos resultado = CalculadoraInformeFacturas.ingresosGastos(List.of());

        assertThat(resultado.ingresos().toPlainString()).isEqualTo("0.00");
        assertThat(resultado.gastos().toPlainString()).isEqualTo("0.00");
        assertThat(resultado.resultado().toPlainString()).isEqualTo("0.00");
    }
}
