package com.facturacion.pagos.internal.domain.service;

import com.facturacion.pagos.internal.domain.exception.ImportePagoExcedidoException;
import com.facturacion.pagos.internal.domain.model.EstadoFactura;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.SituacionFactura;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraEstadoFacturaTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);
    private static final BigDecimal TOTAL = new BigDecimal("121.00");

    private static Pago pago(String importe) {
        return new Pago(null, 10L, 3L, TipoPago.COBRO, HOY, new BigDecimal(importe), MetodoPago.EFECTIVO, null);
    }

    private static SituacionFactura situacion(String pagado, LocalDate fechaVencimiento) {
        return CalculadoraEstadoFactura.situacion(TOTAL, new BigDecimal(pagado), fechaVencimiento, HOY);
    }

    @Test
    void sinPagosNoHayNadaPagadoConEscalaDos() {
        BigDecimal pagado = CalculadoraEstadoFactura.pagado(List.of());

        assertThat(pagado).isEqualByComparingTo("0");
        assertThat(pagado.scale()).isEqualTo(2);
    }

    @Test
    void sumaElImporteDeTodosLosPagos() {
        BigDecimal pagado = CalculadoraEstadoFactura.pagado(List.of(pago("20.10"), pago("0.01"), pago("100")));

        assertThat(pagado.toPlainString()).isEqualTo("120.11");
    }

    @Test
    void sinNadaPagadoLaFacturaEstaPendiente() {
        SituacionFactura situacion = situacion("0.00", null);

        assertThat(situacion.estado()).isEqualTo(EstadoFactura.PENDIENTE);
        assertThat(situacion.total()).isEqualByComparingTo("121.00");
        assertThat(situacion.pagado()).isEqualByComparingTo("0");
        assertThat(situacion.pendiente()).isEqualByComparingTo("121.00");
        assertThat(situacion.vencida()).isFalse();
    }

    @Test
    void conUnaParteDelTotalPagadaLaFacturaEstaParcial() {
        SituacionFactura situacion = situacion("21.00", null);

        assertThat(situacion.estado()).isEqualTo(EstadoFactura.PARCIAL);
        assertThat(situacion.pagado()).isEqualByComparingTo("21.00");
        assertThat(situacion.pendiente()).isEqualByComparingTo("100.00");
    }

    @Test
    void conUnCentimoPendienteLaFacturaSigueParcial() {
        SituacionFactura situacion = situacion("120.99", null);

        assertThat(situacion.estado()).isEqualTo(EstadoFactura.PARCIAL);
        assertThat(situacion.pendiente()).isEqualByComparingTo("0.01");
    }

    @Test
    void conTodoElTotalPagadoLaFacturaEstaPagada() {
        SituacionFactura situacion = situacion("121.00", null);

        assertThat(situacion.estado()).isEqualTo(EstadoFactura.PAGADA);
        assertThat(situacion.pendiente()).isEqualByComparingTo("0");
    }

    @Test
    void unaFacturaDeTotalCeroEstaPagadaYNoVence() {
        SituacionFactura situacion = CalculadoraEstadoFactura.situacion(
                new BigDecimal("0.00"), new BigDecimal("0.00"), HOY.minusDays(30), HOY);

        assertThat(situacion.estado()).isEqualTo(EstadoFactura.PAGADA);
        assertThat(situacion.vencida()).isFalse();
    }

    @Test
    void estaVencidaSiElVencimientoEsAnteriorAHoyYNoEstaPagada() {
        assertThat(situacion("0.00", HOY.minusDays(1)).vencida()).isTrue();
        assertThat(situacion("21.00", HOY.minusDays(1)).vencida()).isTrue();
    }

    @Test
    void noEstaVencidaSiVenceHoy() {
        assertThat(situacion("0.00", HOY).vencida()).isFalse();
    }

    @Test
    void noEstaVencidaSiVenceDespuesDeHoy() {
        assertThat(situacion("0.00", HOY.plusDays(1)).vencida()).isFalse();
    }

    @Test
    void noEstaVencidaSinFechaDeVencimiento() {
        assertThat(situacion("0.00", null).vencida()).isFalse();
    }

    @Test
    void unaFacturaPagadaNoEstaVencidaAunqueSuVencimientoHayaPasado() {
        assertThat(situacion("121.00", HOY.minusDays(1)).vencida()).isFalse();
    }

    @Test
    void admiteUnPagoQueCompletaExactamenteElTotal() {
        assertThatCode(() -> CalculadoraEstadoFactura.exigirQueNoSupereElTotal(
                10L, TOTAL, new BigDecimal("100.00"), new BigDecimal("21.00")))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaUnPagoQueSuperaElTotalEnUnCentimo() {
        assertThatThrownBy(() -> CalculadoraEstadoFactura.exigirQueNoSupereElTotal(
                10L, TOTAL, new BigDecimal("100.00"), new BigDecimal("21.01")))
                .isInstanceOf(ImportePagoExcedidoException.class)
                .hasMessage("El importe del pago supera el pendiente de la factura con id 10 (pendiente: 21.00)");
    }

    @Test
    void rechazaCualquierPagoEnUnaFacturaYaPagada() {
        assertThatThrownBy(() -> CalculadoraEstadoFactura.exigirQueNoSupereElTotal(
                10L, TOTAL, new BigDecimal("121.00"), new BigDecimal("0.01")))
                .isInstanceOf(ImportePagoExcedidoException.class);
    }
}
