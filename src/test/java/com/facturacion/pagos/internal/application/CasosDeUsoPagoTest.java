package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.NuevoPagoDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.ImportePagoExcedidoException;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.exception.PagoNoEncontradoException;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoPagoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final Clock RELOJ = Clock.fixed(HOY.atTime(12, 0).atZone(ZONA).toInstant(), ZONA);
    private static final Long EMPRESA = 3L;
    private static final Long OTRA_EMPRESA = 4L;
    private static final Long EMITIDA = 10L;
    private static final Long RECIBIDA = 11L;

    private final PagoRepositoryEnMemoria repository = new PagoRepositoryEnMemoria();
    private final FacturasEnMemoria facturas = new FacturasEnMemoria();

    private final RegistrarPago registrarPago = new RegistrarPago(repository, facturas, RELOJ);
    private final ObtenerPago obtenerPago = new ObtenerPago(repository);
    private final ListarPagos listarPagos = new ListarPagos(repository);
    private final ConsultarEstadoFacturas consultarEstadoFacturas =
            new ConsultarEstadoFacturas(repository, facturas, RELOJ);
    private final AnularPago anularPago = new AnularPago(repository);

    CasosDeUsoPagoTest() {
        facturas.registrar(FacturasEnMemoria.factura(
                EMITIDA, "EMITIDA", EMPRESA, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 1), "121.00"));
        facturas.registrar(FacturasEnMemoria.factura(
                RECIBIDA, "RECIBIDA", EMPRESA, LocalDate.of(2026, 10, 2), null, "50.00"));
    }

    private static NuevoPagoDto nuevo(Long facturaId, String importe) {
        return new NuevoPagoDto(facturaId, HOY, new BigDecimal(importe), "TRANSFERENCIA", "REF");
    }

    private Pago guardado(Long facturaId, Long empresaId, TipoPago tipo, LocalDate fecha, String importe) {
        return repository.guardar(new Pago(
                null, facturaId, empresaId, tipo, fecha, new BigDecimal(importe), MetodoPago.EFECTIVO, null));
    }

    @Test
    void registrarUnPagoDeUnaFacturaEmitidaCreaUnCobroConLaEmpresaDeLaFactura() {
        PagoDto registrado = registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, HOY.minusDays(2), new BigDecimal("21.5"), "TARJETA", "REF-1"));

        assertThat(registrado).isEqualTo(new PagoDto(
                1L, EMITIDA, EMPRESA, "COBRO", HOY.minusDays(2), new BigDecimal("21.50"), "TARJETA", "REF-1"));
        assertThat(registrado.importe().scale()).isEqualTo(2);
        assertThat(repository.todos()).singleElement().satisfies(pago -> {
            assertThat(pago.tipo()).isEqualTo(TipoPago.COBRO);
            assertThat(pago.empresaId()).isEqualTo(EMPRESA);
        });
    }

    @Test
    void registrarUnPagoDeUnaFacturaRecibidaCreaUnPago() {
        PagoDto registrado = registrarPago.ejecutar(nuevo(RECIBIDA, "10.00"));

        assertThat(registrado.tipo()).isEqualTo("PAGO");
        assertThat(registrado.empresaId()).isEqualTo(EMPRESA);
    }

    @Test
    void registrarAceptaLaFechaDeHoyYUnaReferenciaAusente() {
        PagoDto registrado = registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, HOY, new BigDecimal("1.00"), "OTRO", null));

        assertThat(registrado.fecha()).isEqualTo(HOY);
        assertThat(registrado.referencia()).isNull();
    }

    @Test
    void registrarAdmitePagosParcialesHastaCompletarElTotal() {
        registrarPago.ejecutar(nuevo(EMITIDA, "100.00"));

        PagoDto ultimo = registrarPago.ejecutar(nuevo(EMITIDA, "21.00"));

        assertThat(ultimo.id()).isEqualTo(2L);
        assertThat(repository.listarPorFactura(EMITIDA)).hasSize(2);
    }

    @Test
    void registrarRechazaUnPagoQueSuperaElTotalSinGuardarNada() {
        assertThatThrownBy(() -> registrarPago.ejecutar(nuevo(EMITIDA, "121.01")))
                .isInstanceOf(ImportePagoExcedidoException.class)
                .hasMessage("El importe del pago supera el pendiente de la factura con id 10 (pendiente: 121.00)");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void registrarRechazaUnPagoQueSuperaLoPendienteTrasPagosPrevios() {
        registrarPago.ejecutar(nuevo(EMITIDA, "100.00"));

        assertThatThrownBy(() -> registrarPago.ejecutar(nuevo(EMITIDA, "21.01")))
                .isInstanceOf(ImportePagoExcedidoException.class)
                .hasMessageContaining("(pendiente: 21.00)");
        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void registrarSoloTieneEnCuentaLosPagosDeLaMismaFactura() {
        registrarPago.ejecutar(nuevo(RECIBIDA, "50.00"));

        PagoDto registrado = registrarPago.ejecutar(nuevo(EMITIDA, "121.00"));

        assertThat(registrado.importe()).isEqualByComparingTo("121.00");
    }

    @Test
    void registrarRechazaUnaFacturaInexistenteSinGuardarNada() {
        assertThatThrownBy(() -> registrarPago.ejecutar(nuevo(99L, "1.00")))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("No existe la factura con id 99");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void registrarRechazaUnaFacturaAusente() {
        assertThatThrownBy(() -> registrarPago.ejecutar(nuevo(null, "1.00")))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La factura del pago es obligatoria");
    }

    @Test
    void registrarRechazaUnaFechaPosteriorAHoySegunElReloj() {
        assertThatThrownBy(() -> registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, HOY.plusDays(1), BigDecimal.ONE, "EFECTIVO", null)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La fecha del pago no puede ser posterior a hoy");
        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void registrarRechazaUnaFechaAusente() {
        assertThatThrownBy(() -> registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, null, BigDecimal.ONE, "EFECTIVO", null)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La fecha del pago es obligatoria");
    }

    @Test
    void registrarRechazaUnImporteCero() {
        assertThatThrownBy(() -> registrarPago.ejecutar(nuevo(EMITIDA, "0")))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El importe del pago debe ser mayor que cero");
    }

    @Test
    void registrarRechazaUnMetodoAusente() {
        assertThatThrownBy(() -> registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, HOY, BigDecimal.ONE, null, null)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El método del pago es obligatorio");
    }

    @Test
    void registrarRechazaUnMetodoDesconocido() {
        assertThatThrownBy(() -> registrarPago.ejecutar(
                new NuevoPagoDto(EMITIDA, HOY, BigDecimal.ONE, "CHEQUE", null)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El método del pago debe ser TRANSFERENCIA, TARJETA, EFECTIVO, DOMICILIACION u OTRO");
    }

    @Test
    void registrarAceptaTodosLosMetodos() {
        for (MetodoPago metodo : MetodoPago.values()) {
            PagoDto registrado = registrarPago.ejecutar(
                    new NuevoPagoDto(EMITIDA, HOY, new BigDecimal("1.00"), metodo.name(), null));

            assertThat(registrado.metodo()).isEqualTo(metodo.name());
        }
    }

    @Test
    void obtenerDevuelveElPagoConTodosSusDatos() {
        Long id = guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "12.30").id();

        assertThat(obtenerPago.ejecutar(id)).isEqualTo(new PagoDto(
                id, EMITIDA, EMPRESA, "COBRO", HOY, new BigDecimal("12.30"), "EFECTIVO", null));
    }

    @Test
    void obtenerUnPagoInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> obtenerPago.ejecutar(99L))
                .isInstanceOf(PagoNoEncontradoException.class)
                .hasMessage("No existe el pago con id 99");
    }

    @Test
    void listarPorFacturaDevuelveSoloSusPagosOrdenadosPorFecha() {
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "2.00");
        guardado(RECIBIDA, EMPRESA, TipoPago.PAGO, HOY, "3.00");
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY.minusDays(5), "1.00");

        List<PagoDto> pagos = listarPagos.ejecutar(EMITIDA, null, null);

        assertThat(pagos).extracting(PagoDto::importe)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("1.00"), new BigDecimal("2.00"));
    }

    @Test
    void listarPorEmpresaDevuelveCobrosYPagosDeLaEmpresa() {
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "2.00");
        guardado(RECIBIDA, EMPRESA, TipoPago.PAGO, HOY, "3.00");
        guardado(30L, OTRA_EMPRESA, TipoPago.COBRO, HOY, "4.00");

        List<PagoDto> pagos = listarPagos.ejecutar(null, EMPRESA, null);

        assertThat(pagos).extracting(PagoDto::tipo).containsExactly("COBRO", "PAGO");
    }

    @Test
    void listarPorEmpresaYTipoFiltraPorTipo() {
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "2.00");
        guardado(RECIBIDA, EMPRESA, TipoPago.PAGO, HOY, "3.00");

        assertThat(listarPagos.ejecutar(null, EMPRESA, "COBRO")).extracting(PagoDto::facturaId)
                .containsExactly(EMITIDA);
        assertThat(listarPagos.ejecutar(null, EMPRESA, "PAGO")).extracting(PagoDto::facturaId)
                .containsExactly(RECIBIDA);
    }

    @Test
    void listarConUnTipoDesconocidoLanzaPagoInvalido() {
        assertThatThrownBy(() -> listarPagos.ejecutar(null, EMPRESA, "OTRO"))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El tipo del pago debe ser COBRO o PAGO");
    }

    @Test
    void listarSinFacturaNiEmpresaLanzaPagoInvalido() {
        assertThatThrownBy(() -> listarPagos.ejecutar(null, null, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("Indique facturaId, o bien empresaId con tipo opcional");
    }

    @Test
    void listarConFacturaYEmpresaALaVezLanzaPagoInvalido() {
        assertThatThrownBy(() -> listarPagos.ejecutar(EMITIDA, EMPRESA, null))
                .isInstanceOf(PagoInvalidoException.class);
    }

    @Test
    void listarConFacturaYTipoLanzaPagoInvalido() {
        assertThatThrownBy(() -> listarPagos.ejecutar(EMITIDA, null, "COBRO"))
                .isInstanceOf(PagoInvalidoException.class);
    }

    @Test
    void listarConTipoSinEmpresaLanzaPagoInvalido() {
        assertThatThrownBy(() -> listarPagos.ejecutar(null, null, "COBRO"))
                .isInstanceOf(PagoInvalidoException.class);
    }

    @Test
    void consultarEstadoCalculaPagadoPendienteYEstadoDeCadaFactura() {
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "21.00");

        List<EstadoFacturaDto> estados = consultarEstadoFacturas.ejecutar(EMPRESA, null);

        assertThat(estados).containsExactly(
                new EstadoFacturaDto(
                        EMITIDA, "F-10", "EMITIDA", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 1),
                        new BigDecimal("121.00"), new BigDecimal("21.00"), new BigDecimal("100.00"),
                        "PARCIAL", false),
                new EstadoFacturaDto(
                        RECIBIDA, "F-11", "RECIBIDA", LocalDate.of(2026, 10, 2), null,
                        new BigDecimal("50.00"), new BigDecimal("0.00"), new BigDecimal("50.00"),
                        "PENDIENTE", false));
    }

    @Test
    void consultarEstadoMarcaPagadaLaFacturaCubiertaYVencidaLaQueNoLoEsta() {
        facturas.registrar(FacturasEnMemoria.factura(
                12L, "EMITIDA", EMPRESA, LocalDate.of(2026, 9, 1), HOY.minusDays(1), "10.00"));
        facturas.registrar(FacturasEnMemoria.factura(
                13L, "EMITIDA", EMPRESA, LocalDate.of(2026, 9, 2), HOY.minusDays(1), "10.00"));
        guardado(13L, EMPRESA, TipoPago.COBRO, HOY, "4.00");
        guardado(13L, EMPRESA, TipoPago.COBRO, HOY, "6.00");

        List<EstadoFacturaDto> estados = consultarEstadoFacturas.ejecutar(EMPRESA, null);

        assertThat(estados).filteredOn(estado -> estado.facturaId().equals(12L)).singleElement()
                .satisfies(estado -> {
                    assertThat(estado.estado()).isEqualTo("PENDIENTE");
                    assertThat(estado.vencida()).isTrue();
                });
        assertThat(estados).filteredOn(estado -> estado.facturaId().equals(13L)).singleElement()
                .satisfies(estado -> {
                    assertThat(estado.estado()).isEqualTo("PAGADA");
                    assertThat(estado.pagado()).isEqualByComparingTo("10.00");
                    assertThat(estado.pendiente()).isEqualByComparingTo("0");
                    assertThat(estado.vencida()).isFalse();
                });
    }

    @Test
    void consultarEstadoIgnoraLasFacturasYLosPagosDeOtraEmpresa() {
        facturas.registrar(FacturasEnMemoria.factura(
                30L, "EMITIDA", OTRA_EMPRESA, LocalDate.of(2026, 10, 1), null, "10.00"));
        guardado(30L, OTRA_EMPRESA, TipoPago.COBRO, HOY, "10.00");

        assertThat(consultarEstadoFacturas.ejecutar(EMPRESA, null)).extracting(EstadoFacturaDto::facturaId)
                .containsExactly(EMITIDA, RECIBIDA);
        assertThat(consultarEstadoFacturas.ejecutar(OTRA_EMPRESA, null)).extracting(EstadoFacturaDto::estado)
                .containsExactly("PAGADA");
    }

    @Test
    void consultarEstadoPideLasFacturasSinLimiteDeFechasPracticoEIncluyeLasAntiguas() {
        facturas.registrar(FacturasEnMemoria.factura(
                14L, "EMITIDA", EMPRESA, LocalDate.of(1990, 1, 1), null, "5.00"));

        List<EstadoFacturaDto> estados = consultarEstadoFacturas.ejecutar(EMPRESA, null);

        assertThat(estados).extracting(EstadoFacturaDto::facturaId).containsExactly(14L, EMITIDA, RECIBIDA);
        assertThat(facturas.rangosConsultados())
                .containsExactly(List.of(LocalDate.of(1000, 1, 1), LocalDate.of(9999, 12, 31)));
    }

    @Test
    void consultarEstadoFiltraPorEstado() {
        facturas.registrar(FacturasEnMemoria.factura(
                12L, "EMITIDA", EMPRESA, LocalDate.of(2026, 9, 1), HOY.minusDays(1), "10.00"));
        facturas.registrar(FacturasEnMemoria.factura(
                13L, "EMITIDA", EMPRESA, LocalDate.of(2026, 9, 2), null, "10.00"));
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "21.00");
        guardado(13L, EMPRESA, TipoPago.COBRO, HOY, "10.00");

        assertThat(consultarEstadoFacturas.ejecutar(EMPRESA, "PENDIENTE")).extracting(EstadoFacturaDto::facturaId)
                .containsExactly(12L, RECIBIDA);
        assertThat(consultarEstadoFacturas.ejecutar(EMPRESA, "PARCIAL")).extracting(EstadoFacturaDto::facturaId)
                .containsExactly(EMITIDA);
        assertThat(consultarEstadoFacturas.ejecutar(EMPRESA, "PAGADA")).extracting(EstadoFacturaDto::facturaId)
                .containsExactly(13L);
        assertThat(consultarEstadoFacturas.ejecutar(EMPRESA, "VENCIDA")).extracting(EstadoFacturaDto::facturaId)
                .containsExactly(12L);
    }

    @Test
    void consultarEstadoConUnEstadoDesconocidoLanzaPagoInvalidoSinConsultarFacturas() {
        assertThatThrownBy(() -> consultarEstadoFacturas.ejecutar(EMPRESA, "COBRADA"))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El estado debe ser PENDIENTE, PARCIAL, PAGADA o VENCIDA");
        assertThat(facturas.rangosConsultados()).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "PAGADA")
    void consultarEstadoSinEmpresaLanzaPagoInvalido(String estado) {
        assertThatThrownBy(() -> consultarEstadoFacturas.ejecutar(null, estado))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La empresa es obligatoria para consultar el estado de las facturas");
    }

    @Test
    void consultarEstadoDeUnaEmpresaSinFacturasDevuelveListaVacia() {
        assertThat(consultarEstadoFacturas.ejecutar(99L, null)).isEmpty();
    }

    @Test
    void anularEliminaSoloElPagoIndicado() {
        Long anulado = guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "1.00").id();
        Long conservado = guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "2.00").id();

        anularPago.ejecutar(anulado);

        assertThat(repository.todos()).extracting(Pago::id).containsExactly(conservado);
    }

    @Test
    void anularUnPagoInexistenteLanzaNoEncontradoSinBorrarNada() {
        guardado(EMITIDA, EMPRESA, TipoPago.COBRO, HOY, "1.00");

        assertThatThrownBy(() -> anularPago.ejecutar(99L))
                .isInstanceOf(PagoNoEncontradoException.class)
                .hasMessage("No existe el pago con id 99");
        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void anularUnPagoLiberaSuImporteParaNuevosPagos() {
        Long id = registrarPago.ejecutar(nuevo(EMITIDA, "121.00")).id();

        anularPago.ejecutar(id);

        assertThat(registrarPago.ejecutar(nuevo(EMITIDA, "121.00")).importe()).isEqualByComparingTo("121.00");
    }
}
