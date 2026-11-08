package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.PagoFacade;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagoFacadeImplTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final Clock RELOJ = Clock.fixed(HOY.atStartOfDay(ZONA).toInstant(), ZONA);

    private final PagoRepositoryEnMemoria repository = new PagoRepositoryEnMemoria();
    private final FacturasEnMemoria facturas = new FacturasEnMemoria();
    private final PagoFacade facade = new PagoFacadeImpl(
            new BuscarPago(repository),
            new ListarPagosPorEmpresa(repository),
            new ConsultarEstadoFacturas(repository, facturas, RELOJ));

    private Pago guardado(Long facturaId, Long empresaId, LocalDate fecha, String importe) {
        return repository.guardar(new Pago(
                null, facturaId, empresaId, TipoPago.COBRO, fecha, new BigDecimal(importe),
                MetodoPago.DOMICILIACION, "REF"));
    }

    @Test
    void buscarDevuelveTodosLosDatosDelPago() {
        Long id = guardado(10L, 3L, HOY, "15.00").id();

        assertThat(facade.buscar(id)).contains(new PagoDto(
                id, 10L, 3L, "COBRO", HOY, new BigDecimal("15.00"), "DOMICILIACION", "REF"));
    }

    @Test
    void buscarUnPagoInexistenteDevuelveVacioSinLanzarExcepcion() {
        assertThat(facade.buscar(99L)).isEmpty();
    }

    @Test
    void listarPorEmpresaIncluyeAmbosExtremosDelRangoDeFechas() {
        LocalDate desde = LocalDate.of(2026, 10, 1);
        LocalDate hasta = LocalDate.of(2026, 10, 31);
        guardado(10L, 3L, desde.minusDays(1), "1.00");
        Long primero = guardado(10L, 3L, desde, "2.00").id();
        Long ultimo = guardado(10L, 3L, hasta, "3.00").id();
        guardado(10L, 3L, hasta.plusDays(1), "4.00");
        guardado(20L, 4L, desde, "5.00");

        assertThat(facade.listarPorEmpresa(3L, desde, hasta)).extracting(PagoDto::id)
                .containsExactly(primero, ultimo);
    }

    @Test
    void estadosPorEmpresaDevuelveElEstadoDeTodasSusFacturasSinFiltrar() {
        facturas.registrar(FacturasEnMemoria.factura(
                10L, "EMITIDA", 3L, LocalDate.of(2026, 10, 1), HOY.minusDays(1), "100.00"));
        facturas.registrar(FacturasEnMemoria.factura(
                11L, "RECIBIDA", 3L, LocalDate.of(2026, 10, 2), null, "40.00"));
        guardado(11L, 3L, HOY, "40.00");

        assertThat(facade.estadosPorEmpresa(3L)).containsExactly(
                new EstadoFacturaDto(
                        10L, "F-10", "EMITIDA", LocalDate.of(2026, 10, 1), HOY.minusDays(1),
                        new BigDecimal("100.00"), new BigDecimal("0.00"), new BigDecimal("100.00"),
                        "PENDIENTE", true),
                new EstadoFacturaDto(
                        11L, "F-11", "RECIBIDA", LocalDate.of(2026, 10, 2), null,
                        new BigDecimal("40.00"), new BigDecimal("40.00"), new BigDecimal("0.00"),
                        "PAGADA", false));
    }

    @Test
    void estadosPorEmpresaSinEmpresaLanzaPagoInvalido() {
        assertThatThrownBy(() -> facade.estadosPorEmpresa(null)).isInstanceOf(PagoInvalidoException.class);
    }
}
