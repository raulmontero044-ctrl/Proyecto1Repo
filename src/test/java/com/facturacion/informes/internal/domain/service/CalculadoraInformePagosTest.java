package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.EstadoCobro;
import com.facturacion.informes.internal.domain.model.PagoResumido;
import com.facturacion.informes.internal.domain.model.ResumenPendientes;
import com.facturacion.informes.internal.domain.model.ResumenTesoreria;
import com.facturacion.informes.internal.domain.model.SaldoFactura;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TipoPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraInformePagosTest {

    private static final TipoFactura EMITIDA = TipoFactura.EMITIDA;
    private static final TipoFactura RECIBIDA = TipoFactura.RECIBIDA;

    private static SaldoFactura saldo(
            Long id, TipoFactura tipo, String pendiente, EstadoCobro estado, boolean vencida) {
        return new SaldoFactura(
                id, "F-" + id, tipo, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), new BigDecimal("500.00"),
                new BigDecimal("500.00").subtract(new BigDecimal(pendiente)), new BigDecimal(pendiente), estado,
                vencida);
    }

    private static PagoResumido pago(Long id, TipoPago tipo, String importe) {
        return new PagoResumido(id, tipo, new BigDecimal(importe));
    }

    @Test
    void sumaLoPendienteDeCobroDeLasEmitidasYLoPendienteDePagoDeLasRecibidas() {
        List<SaldoFactura> saldos = List.of(
                saldo(1L, EMITIDA, "100.00", EstadoCobro.PENDIENTE, false),
                saldo(2L, EMITIDA, "50.25", EstadoCobro.PARCIAL, false),
                saldo(3L, RECIBIDA, "30.10", EstadoCobro.PENDIENTE, false));

        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(saldos);

        assertThat(pendientes.pendienteCobro().toPlainString()).isEqualTo("150.25");
        assertThat(pendientes.pendientePago().toPlainString()).isEqualTo("30.10");
    }

    @Test
    void loVencidoSoloSumaLoPendienteDeLasFacturasVencidasDeCadaTipo() {
        List<SaldoFactura> saldos = List.of(
                saldo(1L, EMITIDA, "100.00", EstadoCobro.PENDIENTE, true),
                saldo(2L, EMITIDA, "40.00", EstadoCobro.PARCIAL, false),
                saldo(3L, EMITIDA, "7.50", EstadoCobro.PARCIAL, true),
                saldo(4L, RECIBIDA, "20.00", EstadoCobro.PENDIENTE, false),
                saldo(5L, RECIBIDA, "12.30", EstadoCobro.PENDIENTE, true));

        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(saldos);

        assertThat(pendientes.pendienteCobro().toPlainString()).isEqualTo("147.50");
        assertThat(pendientes.vencidoCobro().toPlainString()).isEqualTo("107.50");
        assertThat(pendientes.pendientePago().toPlainString()).isEqualTo("32.30");
        assertThat(pendientes.vencidoPago().toPlainString()).isEqualTo("12.30");
    }

    @Test
    void elDetalleExcluyeLasFacturasPagadasYConservaElOrdenRecibido() {
        List<SaldoFactura> saldos = List.of(
                saldo(4L, RECIBIDA, "20.00", EstadoCobro.PENDIENTE, false),
                saldo(1L, EMITIDA, "0.00", EstadoCobro.PAGADA, false),
                saldo(2L, EMITIDA, "40.00", EstadoCobro.PARCIAL, true),
                saldo(3L, RECIBIDA, "0.00", EstadoCobro.PAGADA, false));

        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(saldos);

        assertThat(pendientes.facturas()).extracting(SaldoFactura::facturaId).containsExactly(4L, 2L);
    }

    @Test
    void lasFacturasPagadasNoSumanAunqueTraiganPendienteOVencida() {
        List<SaldoFactura> saldos = List.of(
                saldo(1L, EMITIDA, "9.99", EstadoCobro.PAGADA, true),
                saldo(2L, RECIBIDA, "8.88", EstadoCobro.PAGADA, true));

        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(saldos);

        assertThat(pendientes.pendienteCobro()).isEqualByComparingTo("0");
        assertThat(pendientes.vencidoCobro()).isEqualByComparingTo("0");
        assertThat(pendientes.pendientePago()).isEqualByComparingTo("0");
        assertThat(pendientes.vencidoPago()).isEqualByComparingTo("0");
        assertThat(pendientes.facturas()).isEmpty();
    }

    @Test
    void sinFacturasLosPendientesSonCeroConEscalaDos() {
        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(List.of());

        assertThat(pendientes.pendienteCobro().toPlainString()).isEqualTo("0.00");
        assertThat(pendientes.vencidoCobro().toPlainString()).isEqualTo("0.00");
        assertThat(pendientes.pendientePago().toPlainString()).isEqualTo("0.00");
        assertThat(pendientes.vencidoPago().toPlainString()).isEqualTo("0.00");
        assertThat(pendientes.facturas()).isEmpty();
    }

    @Test
    void lasSumasDePendientesSeRedondeanADosDecimalesConHalfUp() {
        List<SaldoFactura> saldos = List.of(
                saldo(1L, EMITIDA, "1.115", EstadoCobro.PENDIENTE, true),
                saldo(2L, EMITIDA, "1.110", EstadoCobro.PENDIENTE, true),
                saldo(3L, RECIBIDA, "5", EstadoCobro.PENDIENTE, false));

        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(saldos);

        assertThat(pendientes.pendienteCobro().toPlainString()).isEqualTo("2.23");
        assertThat(pendientes.vencidoCobro().toPlainString()).isEqualTo("2.23");
        assertThat(pendientes.pendientePago().toPlainString()).isEqualTo("5.00");
        assertThat(pendientes.vencidoPago().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void elDetalleDePendientesEsInmutable() {
        ResumenPendientes pendientes = CalculadoraInformePagos.pendientes(
                List.of(saldo(1L, EMITIDA, "1.00", EstadoCobro.PENDIENTE, false)));

        assertThatThrownBy(() -> pendientes.facturas().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void laTesoreriaSumaCobrosYPagosYElSaldoNetoEsSuDiferencia() {
        List<PagoResumido> pagos = List.of(
                pago(1L, TipoPago.COBRO, "121.00"),
                pago(2L, TipoPago.PAGO, "36.30"),
                pago(3L, TipoPago.COBRO, "10.50"),
                pago(4L, TipoPago.PAGO, "4.20"));

        ResumenTesoreria tesoreria = CalculadoraInformePagos.tesoreria(pagos);

        assertThat(tesoreria.cobros().toPlainString()).isEqualTo("131.50");
        assertThat(tesoreria.pagos().toPlainString()).isEqualTo("40.50");
        assertThat(tesoreria.saldoNeto().toPlainString()).isEqualTo("91.00");
    }

    @Test
    void elSaldoNetoEsNegativoCuandoLosPagosSuperanALosCobros() {
        ResumenTesoreria tesoreria = CalculadoraInformePagos.tesoreria(List.of(
                pago(1L, TipoPago.COBRO, "10.00"),
                pago(2L, TipoPago.PAGO, "25.10")));

        assertThat(tesoreria.saldoNeto().toPlainString()).isEqualTo("-15.10");
    }

    @Test
    void sinPagosLaTesoreriaEsCeroConEscalaDos() {
        ResumenTesoreria tesoreria = CalculadoraInformePagos.tesoreria(List.of());

        assertThat(tesoreria.cobros().toPlainString()).isEqualTo("0.00");
        assertThat(tesoreria.pagos().toPlainString()).isEqualTo("0.00");
        assertThat(tesoreria.saldoNeto().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void lasSumasDeTesoreriaSeRedondeanADosDecimalesConHalfUp() {
        ResumenTesoreria tesoreria = CalculadoraInformePagos.tesoreria(List.of(
                pago(1L, TipoPago.COBRO, "1.115"),
                pago(2L, TipoPago.COBRO, "1.110"),
                pago(3L, TipoPago.PAGO, "3")));

        assertThat(tesoreria.cobros().toPlainString()).isEqualTo("2.23");
        assertThat(tesoreria.pagos().toPlainString()).isEqualTo("3.00");
        assertThat(tesoreria.saldoNeto().toPlainString()).isEqualTo("-0.77");
    }
}
