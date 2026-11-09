package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import com.facturacion.informes.internal.domain.model.EstadisticaPeriodo;
import com.facturacion.informes.internal.domain.model.EstadoCobro;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.PagoResumido;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.ResumenImpuestos;
import com.facturacion.informes.internal.domain.model.ResumenPendientes;
import com.facturacion.informes.internal.domain.model.ResumenTesoreria;
import com.facturacion.informes.internal.domain.model.SaldoFactura;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TipoPago;
import com.facturacion.informes.internal.domain.model.TramoIva;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InformeDtoConverterTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);
    private static final LocalDate VENCIMIENTO = LocalDate.of(2026, 4, 15);

    private static FacturaDto facturaConTipo(String tipo) {
        return new FacturaDto(
                7L, "F-7", tipo, FECHA, VENCIMIENTO, 3L, "Empresa SL", "B00000003", "Calle Mayor 1", 20L,
                "Tercero SA", "A00000020", "Avenida Diagonal 2", new BigDecimal("21.00"), List.of(),
                new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"));
    }

    private static EstadoFacturaDto estado(String tipo, String estado) {
        return new EstadoFacturaDto(
                7L, "F-7", tipo, FECHA, VENCIMIENTO, new BigDecimal("121.00"), new BigDecimal("21.00"),
                new BigDecimal("100.00"), estado, true);
    }

    private static PagoDto pago(String tipo) {
        return new PagoDto(30L, 7L, 3L, tipo, FECHA, new BigDecimal("21.00"), "TRANSFERENCIA", "REF-1");
    }

    @Test
    void laEmpresaObligatoriaSeDevuelveTalCual() {
        assertThat(InformeDtoConverter.empresaObligatoria(3L)).isEqualTo(3L);
    }

    @Test
    void rechazaQueFalteLaEmpresa() {
        assertThatThrownBy(() -> InformeDtoConverter.empresaObligatoria(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
    }

    @Test
    void convierteLaFacturaDelContratoConservandoSusImportes() {
        List<FacturaResumida> facturas = InformeDtoConverter.aFacturasResumidas(List.of(facturaConTipo("RECIBIDA")));

        assertThat(facturas).containsExactly(new FacturaResumida(
                7L, "F-7", TipoFactura.RECIBIDA, FECHA, 20L, "Tercero SA", new BigDecimal("21.00"),
                new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00")));
    }

    @Test
    void rechazaUnaFacturaDeTipoDesconocido() {
        assertThatThrownBy(() -> InformeDtoConverter.aFacturasResumidas(List.of(facturaConTipo("PROFORMA"))))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }

    @Test
    void convierteElEstadoDeLaFacturaConservandoSusImportesYSiEstaVencida() {
        List<SaldoFactura> saldos = InformeDtoConverter.aSaldosFactura(List.of(estado("EMITIDA", "PARCIAL")));

        assertThat(saldos).containsExactly(new SaldoFactura(
                7L, "F-7", TipoFactura.EMITIDA, FECHA, VENCIMIENTO, new BigDecimal("121.00"),
                new BigDecimal("21.00"), new BigDecimal("100.00"), EstadoCobro.PARCIAL, true));
    }

    @Test
    void rechazaUnEstadoDeFacturaDesconocido() {
        assertThatThrownBy(() -> InformeDtoConverter.aSaldosFactura(List.of(estado("EMITIDA", "ANULADA"))))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El estado de la factura debe ser PENDIENTE, PARCIAL o PAGADA");
    }

    @Test
    void convierteElPagoDelContratoEnUnPagoResumido() {
        List<PagoResumido> pagos = InformeDtoConverter.aPagosResumidos(List.of(pago("PAGO"), pago("COBRO")));

        assertThat(pagos).containsExactly(
                new PagoResumido(30L, TipoPago.PAGO, new BigDecimal("21.00")),
                new PagoResumido(30L, TipoPago.COBRO, new BigDecimal("21.00")));
    }

    @Test
    void rechazaUnPagoDeTipoDesconocido() {
        assertThatThrownBy(() -> InformeDtoConverter.aPagosResumidos(List.of(pago("DEVOLUCION"))))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo del pago debe ser COBRO o PAGO");
    }

    @Test
    void convierteElResumenDeFacturasConElTipoComoTexto() {
        FacturaResumida factura = new FacturaResumida(
                7L, "F-7", TipoFactura.EMITIDA, FECHA, 20L, "Tercero SA", new BigDecimal("21.00"),
                new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"));
        ResumenFacturas resumen = new ResumenFacturas(
                List.of(factura), 1, new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"));

        assertThat(InformeDtoConverter.aDto(resumen)).isEqualTo(new ResumenFacturasDto(
                List.of(new FacturaResumidaDto(
                        7L, "F-7", "EMITIDA", FECHA, 20L, "Tercero SA", new BigDecimal("21.00"),
                        new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"))),
                1, new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00")));
    }

    @Test
    void convierteIngresosYGastos() {
        IngresosGastos ingresosGastos = new IngresosGastos(
                new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("6.00"));

        assertThat(InformeDtoConverter.aDto(ingresosGastos)).isEqualTo(new IngresosGastosDto(
                new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("6.00")));
    }

    @Test
    void convierteElResumenDeImpuestosConSusTramos() {
        ResumenImpuestos impuestos = new ResumenImpuestos(
                List.of(new TramoIva(new BigDecimal("21.00"), new BigDecimal("100.00"), new BigDecimal("21.00"))),
                new BigDecimal("21.00"),
                List.of(new TramoIva(new BigDecimal("4.00"), new BigDecimal("10.00"), new BigDecimal("0.40"))),
                new BigDecimal("0.40"),
                new BigDecimal("20.60"));

        assertThat(InformeDtoConverter.aDto(impuestos)).isEqualTo(new ResumenImpuestosDto(
                List.of(new TramoIvaDto(new BigDecimal("21.00"), new BigDecimal("100.00"), new BigDecimal("21.00"))),
                new BigDecimal("21.00"),
                List.of(new TramoIvaDto(new BigDecimal("4.00"), new BigDecimal("10.00"), new BigDecimal("0.40"))),
                new BigDecimal("0.40"),
                new BigDecimal("20.60")));
    }

    @Test
    void convierteLasEstadisticasConservandoElOrden() {
        List<EstadisticaPeriodo> estadisticas = List.of(
                new EstadisticaPeriodo("2026-T1", 2, new BigDecimal("176.00"), 1, new BigDecimal("48.40")),
                new EstadisticaPeriodo("2026-T2", 0, new BigDecimal("0.00"), 3, new BigDecimal("9.00")));

        assertThat(InformeDtoConverter.aEstadisticasDto(estadisticas)).containsExactly(
                new EstadisticaPeriodoDto("2026-T1", 2, new BigDecimal("176.00"), 1, new BigDecimal("48.40")),
                new EstadisticaPeriodoDto("2026-T2", 0, new BigDecimal("0.00"), 3, new BigDecimal("9.00")));
    }

    @Test
    void convierteLosPendientesConElTipoYElEstadoComoTexto() {
        SaldoFactura saldo = new SaldoFactura(
                7L, "F-7", TipoFactura.RECIBIDA, FECHA, null, new BigDecimal("121.00"), new BigDecimal("21.00"),
                new BigDecimal("100.00"), EstadoCobro.PARCIAL, true);
        ResumenPendientes pendientes = new ResumenPendientes(
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("100.00"), new BigDecimal("100.00"),
                List.of(saldo));

        assertThat(InformeDtoConverter.aDto(pendientes)).isEqualTo(new ResumenPendientesDto(
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("100.00"), new BigDecimal("100.00"),
                List.of(new SaldoFacturaDto(
                        7L, "F-7", "RECIBIDA", FECHA, null, new BigDecimal("121.00"), new BigDecimal("21.00"),
                        new BigDecimal("100.00"), "PARCIAL", true))));
    }

    @Test
    void convierteLaTesoreria() {
        ResumenTesoreria tesoreria = new ResumenTesoreria(
                new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("6.00"));

        assertThat(InformeDtoConverter.aDto(tesoreria)).isEqualTo(new ResumenTesoreriaDto(
                new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("6.00")));
    }
}
