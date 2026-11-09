package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoInformeTest {

    private static final Long EMPRESA = 3L;
    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 3, 31);

    private final FacadesEnMemoria facades = new FacadesEnMemoria();
    private final ConsultarLibroFacturas consultarLibroFacturas =
            new ConsultarLibroFacturas(facades.facturaFacade());
    private final ConsultarIngresosGastos consultarIngresosGastos =
            new ConsultarIngresosGastos(facades.facturaFacade());
    private final ConsultarImpuestos consultarImpuestos = new ConsultarImpuestos(facades.facturaFacade());
    private final ConsultarEstadisticas consultarEstadisticas = new ConsultarEstadisticas(facades.facturaFacade());
    private final ConsultarPendientes consultarPendientes = new ConsultarPendientes(facades.pagoFacade());
    private final ConsultarTesoreria consultarTesoreria = new ConsultarTesoreria(facades.pagoFacade());

    CasosDeUsoInformeTest() {
        facades.registrar(factura(1L, "EMITIDA", LocalDate.of(2026, 2, 10), "21.00", "100.00", "21.00", "121.00"));
        facades.registrar(factura(2L, "RECIBIDA", LocalDate.of(2026, 1, 20), "21.00", "40.00", "8.40", "48.40"));
        facades.registrar(factura(3L, "EMITIDA", LocalDate.of(2026, 1, 5), "10.00", "50.00", "5.00", "55.00"));
        facades.registrar(factura(4L, "EMITIDA", LocalDate.of(2026, 4, 1), "21.00", "999.00", "209.79", "1208.79"));
        facades.registrar(facturaDeOtraEmpresa(5L));
        facades.registrar(estado(1L, "EMITIDA", "121.00", "0.00", "PENDIENTE", true));
        facades.registrar(estado(2L, "RECIBIDA", "48.40", "48.40", "PAGADA", false));
        facades.registrar(estado(3L, "EMITIDA", "55.00", "25.00", "PARCIAL", false));
        facades.registrar(estado(4L, "RECIBIDA", "60.00", "10.00", "PARCIAL", true));
        facades.registrar(pago(1L, EMPRESA, "COBRO", LocalDate.of(2026, 2, 1), "121.00"));
        facades.registrar(pago(2L, EMPRESA, "PAGO", LocalDate.of(2026, 3, 31), "48.40"));
        facades.registrar(pago(3L, EMPRESA, "COBRO", LocalDate.of(2026, 1, 1), "25.00"));
        facades.registrar(pago(4L, EMPRESA, "COBRO", LocalDate.of(2026, 4, 1), "500.00"));
        facades.registrar(pago(5L, 4L, "PAGO", LocalDate.of(2026, 2, 1), "700.00"));
    }

    private static EstadoFacturaDto estado(
            Long facturaId, String tipo, String total, String pagado, String estado, boolean vencida) {
        BigDecimal pendiente = new BigDecimal(total).subtract(new BigDecimal(pagado));
        return new EstadoFacturaDto(
                facturaId, "F-" + facturaId, tipo, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 2, 10),
                new BigDecimal(total), new BigDecimal(pagado), pendiente, estado, vencida);
    }

    private static PagoDto pago(Long id, Long empresaId, String tipo, LocalDate fecha, String importe) {
        return new PagoDto(id, 1L, empresaId, tipo, fecha, new BigDecimal(importe), "TRANSFERENCIA", null);
    }

    private static FacturaDto factura(
            Long id, String tipo, LocalDate fecha, String porcentaje, String base, String cuota, String total) {
        return factura(id, EMPRESA, tipo, fecha, porcentaje, base, cuota, total);
    }

    private static FacturaDto facturaDeOtraEmpresa(Long id) {
        return factura(id, 4L, "EMITIDA", LocalDate.of(2026, 2, 1), "21.00", "700.00", "147.00", "847.00");
    }

    private static FacturaDto factura(
            Long id, Long empresaId, String tipo, LocalDate fecha, String porcentaje, String base, String cuota,
            String total) {
        return new FacturaDto(
                id, "F-" + id, tipo, fecha, null, empresaId, "Empresa SL", "B00000003", null, 20L, "Tercero SA",
                "A00000020", null, new BigDecimal(porcentaje), List.of(), new BigDecimal(base),
                new BigDecimal(cuota), new BigDecimal(total));
    }

    @Test
    void elLibroDeEmitidasListaLasDelPeriodoOrdenadasPorFechaConSusTotales() {
        ResumenFacturasDto libro = consultarLibroFacturas.ejecutar(EMPRESA, DESDE, HASTA, "EMITIDA");

        assertThat(libro.facturas()).containsExactly(
                new FacturaResumidaDto(
                        3L, "F-3", "EMITIDA", LocalDate.of(2026, 1, 5), 20L, "Tercero SA", new BigDecimal("10.00"),
                        new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("55.00")),
                new FacturaResumidaDto(
                        1L, "F-1", "EMITIDA", LocalDate.of(2026, 2, 10), 20L, "Tercero SA", new BigDecimal("21.00"),
                        new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00")));
        assertThat(libro.numeroFacturas()).isEqualTo(2);
        assertThat(libro.base().toPlainString()).isEqualTo("150.00");
        assertThat(libro.cuotaIva().toPlainString()).isEqualTo("26.00");
        assertThat(libro.total().toPlainString()).isEqualTo("176.00");
    }

    @Test
    void elLibroPideALaFacadeDeFacturaLaEmpresaYElPeriodoIndicados() {
        consultarLibroFacturas.ejecutar(EMPRESA, DESDE, HASTA, "RECIBIDA");

        assertThat(facades.consultas()).containsExactly("facturas 3 2026-01-01 2026-03-31");
    }

    @Test
    void elLibroDeRecibidasSoloIncluyeLasRecibidas() {
        ResumenFacturasDto libro = consultarLibroFacturas.ejecutar(EMPRESA, DESDE, HASTA, "RECIBIDA");

        assertThat(libro.facturas()).extracting(FacturaResumidaDto::id).containsExactly(2L);
        assertThat(libro.total().toPlainString()).isEqualTo("48.40");
    }

    @Test
    void unLibroSinFacturasDevuelveCerosConEscalaDos() {
        ResumenFacturasDto libro = consultarLibroFacturas.ejecutar(
                EMPRESA, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "EMITIDA");

        assertThat(libro.facturas()).isEmpty();
        assertThat(libro.numeroFacturas()).isZero();
        assertThat(libro.base().toPlainString()).isEqualTo("0.00");
        assertThat(libro.cuotaIva().toPlainString()).isEqualTo("0.00");
        assertThat(libro.total().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void elLibroAceptaUnPeriodoDeUnSoloDia() {
        LocalDate dia = LocalDate.of(2026, 2, 10);

        ResumenFacturasDto libro = consultarLibroFacturas.ejecutar(EMPRESA, dia, dia, "EMITIDA");

        assertThat(libro.facturas()).extracting(FacturaResumidaDto::id).containsExactly(1L);
    }

    @Test
    void elLibroRechazaUnTipoDesconocidoSinConsultarFacturas() {
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(EMPRESA, DESDE, HASTA, "PROFORMA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void elLibroRechazaQueFalteElTipo() {
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(EMPRESA, DESDE, HASTA, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void elLibroRechazaQueFalteLaEmpresa() {
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(null, DESDE, HASTA, "EMITIDA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void elLibroRechazaUnPeriodoConDesdePosteriorAHasta() {
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(EMPRESA, HASTA.plusDays(1), HASTA, "EMITIDA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void elLibroRechazaQueFalteAlgunaFecha() {
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(EMPRESA, null, HASTA, "EMITIDA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("Las fechas desde y hasta del informe son obligatorias");
        assertThatThrownBy(() -> consultarLibroFacturas.ejecutar(EMPRESA, DESDE, null, "EMITIDA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("Las fechas desde y hasta del informe son obligatorias");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void ingresosYGastosSonLasBasesDeEmitidasYRecibidasDelPeriodo() {
        IngresosGastosDto resultado = consultarIngresosGastos.ejecutar(EMPRESA, DESDE, HASTA);

        assertThat(resultado.ingresos().toPlainString()).isEqualTo("150.00");
        assertThat(resultado.gastos().toPlainString()).isEqualTo("40.00");
        assertThat(resultado.resultado().toPlainString()).isEqualTo("110.00");
        assertThat(facades.consultas()).containsExactly("facturas 3 2026-01-01 2026-03-31");
    }

    @Test
    void ingresosYGastosSinFacturasSonCeroConEscalaDos() {
        IngresosGastosDto resultado = consultarIngresosGastos.ejecutar(
                EMPRESA, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(resultado).isEqualTo(new IngresosGastosDto(
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00")));
    }

    @Test
    void ingresosYGastosRechazanParametrosInvalidosSinConsultarFacturas() {
        assertThatThrownBy(() -> consultarIngresosGastos.ejecutar(null, DESDE, HASTA))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThatThrownBy(() -> consultarIngresosGastos.ejecutar(EMPRESA, HASTA, DESDE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void losImpuestosAgrupanElIvaRepercutidoYSoportadoDelPeriodoPorTramo() {
        ResumenImpuestosDto impuestos = consultarImpuestos.ejecutar(EMPRESA, DESDE, HASTA);

        assertThat(impuestos).isEqualTo(new ResumenImpuestosDto(
                List.of(
                        new TramoIvaDto(new BigDecimal("10.00"), new BigDecimal("50.00"), new BigDecimal("5.00")),
                        new TramoIvaDto(new BigDecimal("21.00"), new BigDecimal("100.00"), new BigDecimal("21.00"))),
                new BigDecimal("26.00"),
                List.of(new TramoIvaDto(new BigDecimal("21.00"), new BigDecimal("40.00"), new BigDecimal("8.40"))),
                new BigDecimal("8.40"),
                new BigDecimal("17.60")));
        assertThat(facades.consultas()).containsExactly("facturas 3 2026-01-01 2026-03-31");
    }

    @Test
    void losImpuestosSinFacturasNoTienenTramosYSonCero() {
        ResumenImpuestosDto impuestos = consultarImpuestos.ejecutar(
                EMPRESA, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(impuestos).isEqualTo(new ResumenImpuestosDto(
                List.of(), new BigDecimal("0.00"), List.of(), new BigDecimal("0.00"), new BigDecimal("0.00")));
    }

    @Test
    void losImpuestosRechazanParametrosInvalidosSinConsultarFacturas() {
        assertThatThrownBy(() -> consultarImpuestos.ejecutar(null, DESDE, HASTA))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThatThrownBy(() -> consultarImpuestos.ejecutar(EMPRESA, HASTA, DESDE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void lasEstadisticasMensualesSoloIncluyenLosMesesConFacturas() {
        List<EstadisticaPeriodoDto> estadisticas = consultarEstadisticas.ejecutar(EMPRESA, DESDE, HASTA, "MES");

        assertThat(estadisticas).containsExactly(
                new EstadisticaPeriodoDto("2026-01", 1, new BigDecimal("55.00"), 1, new BigDecimal("48.40")),
                new EstadisticaPeriodoDto("2026-02", 1, new BigDecimal("121.00"), 0, new BigDecimal("0.00")));
        assertThat(facades.consultas()).containsExactly("facturas 3 2026-01-01 2026-03-31");
    }

    @Test
    void lasEstadisticasTrimestralesYAnualesUsanSusClavesDePeriodo() {
        assertThat(consultarEstadisticas.ejecutar(EMPRESA, DESDE, HASTA, "TRIMESTRE")).containsExactly(
                new EstadisticaPeriodoDto("2026-T1", 2, new BigDecimal("176.00"), 1, new BigDecimal("48.40")));
        assertThat(consultarEstadisticas.ejecutar(EMPRESA, DESDE, HASTA, "ANIO")).containsExactly(
                new EstadisticaPeriodoDto("2026", 2, new BigDecimal("176.00"), 1, new BigDecimal("48.40")));
    }

    @Test
    void lasEstadisticasSinFacturasEstanVacias() {
        assertThat(consultarEstadisticas.ejecutar(
                EMPRESA, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), "MES")).isEmpty();
    }

    @Test
    void lasEstadisticasRechazanUnaAgrupacionDesconocidaOAusenteSinConsultarFacturas() {
        assertThatThrownBy(() -> consultarEstadisticas.ejecutar(EMPRESA, DESDE, HASTA, "SEMANA"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La agrupación del informe debe ser MES, TRIMESTRE o ANIO");
        assertThatThrownBy(() -> consultarEstadisticas.ejecutar(EMPRESA, DESDE, HASTA, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La agrupación del informe es obligatoria");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void lasEstadisticasRechazanParametrosComunesInvalidos() {
        assertThatThrownBy(() -> consultarEstadisticas.ejecutar(null, DESDE, HASTA, "MES"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThatThrownBy(() -> consultarEstadisticas.ejecutar(EMPRESA, HASTA, DESDE, "MES"))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void losPendientesSumanLoPendienteYLoVencidoDeCobroYDePagoConElDetalleDeLasNoPagadas() {
        ResumenPendientesDto pendientes = consultarPendientes.ejecutar(EMPRESA);

        assertThat(pendientes.pendienteCobro().toPlainString()).isEqualTo("151.00");
        assertThat(pendientes.vencidoCobro().toPlainString()).isEqualTo("121.00");
        assertThat(pendientes.pendientePago().toPlainString()).isEqualTo("50.00");
        assertThat(pendientes.vencidoPago().toPlainString()).isEqualTo("50.00");
        assertThat(pendientes.facturas()).extracting(SaldoFacturaDto::facturaId).containsExactly(1L, 3L, 4L);
        assertThat(pendientes.facturas().get(1)).isEqualTo(new SaldoFacturaDto(
                3L, "F-3", "EMITIDA", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 2, 10),
                new BigDecimal("55.00"), new BigDecimal("25.00"), new BigDecimal("30.00"), "PARCIAL", false));
        assertThat(facades.consultas()).containsExactly("estados 3");
    }

    @Test
    void losPendientesRechazanQueFalteLaEmpresaSinConsultarPagos() {
        assertThatThrownBy(() -> consultarPendientes.ejecutar(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThat(facades.consultas()).isEmpty();
    }

    @Test
    void laTesoreriaSumaCobrosYPagosDelPeriodoIncluidosSusExtremos() {
        ResumenTesoreriaDto tesoreria = consultarTesoreria.ejecutar(EMPRESA, DESDE, HASTA);

        assertThat(tesoreria).isEqualTo(new ResumenTesoreriaDto(
                new BigDecimal("146.00"), new BigDecimal("48.40"), new BigDecimal("97.60")));
        assertThat(facades.consultas()).containsExactly("pagos 3 2026-01-01 2026-03-31");
    }

    @Test
    void laTesoreriaSinPagosEsCeroConEscalaDos() {
        ResumenTesoreriaDto tesoreria = consultarTesoreria.ejecutar(
                EMPRESA, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(tesoreria).isEqualTo(new ResumenTesoreriaDto(
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00")));
    }

    @Test
    void laTesoreriaRechazaParametrosInvalidosSinConsultarPagos() {
        assertThatThrownBy(() -> consultarTesoreria.ejecutar(null, DESDE, HASTA))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La empresa del informe es obligatoria");
        assertThatThrownBy(() -> consultarTesoreria.ejecutar(EMPRESA, HASTA, DESDE))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
        assertThatThrownBy(() -> consultarTesoreria.ejecutar(EMPRESA, DESDE, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("Las fechas desde y hasta del informe son obligatorias");
        assertThat(facades.consultas()).isEmpty();
    }
}
