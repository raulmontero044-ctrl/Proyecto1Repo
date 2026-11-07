package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaLineaFacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoFacturaTest {

    private final FacturaRepositoryEnMemoria repository = new FacturaRepositoryEnMemoria();
    private final CrearFactura crear = new CrearFactura(repository);
    private final ObtenerFactura obtener = new ObtenerFactura(repository);
    private final ListarFacturas listar = new ListarFacturas(repository);
    private final BuscarFactura buscar = new BuscarFactura(repository);
    private final List<Object> eventos = new ArrayList<>();
    private final EliminarFactura eliminar = new EliminarFactura(repository, eventos::add);

    private static NuevaFacturaDto nueva(String numero, List<NuevaLineaFacturaDto> lineas) {
        return nueva(numero, "EMITIDA", lineas);
    }

    private static NuevaFacturaDto nueva(String numero, String tipo, List<NuevaLineaFacturaDto> lineas) {
        return nueva(numero, tipo, null, lineas);
    }

    private static NuevaFacturaDto nueva(
            String numero, String tipo, LocalDate fechaVencimiento, List<NuevaLineaFacturaDto> lineas) {
        return new NuevaFacturaDto(
                numero,
                tipo,
                LocalDate.of(2026, 11, 3),
                fechaVencimiento,
                "Cliente SL",
                "B12345678",
                new BigDecimal("21"),
                lineas);
    }

    private static List<NuevaLineaFacturaDto> dosLineas() {
        return List.of(
                new NuevaLineaFacturaDto("Primera", new BigDecimal("2"), new BigDecimal("10.50")),
                new NuevaLineaFacturaDto("Segunda", new BigDecimal("1"), new BigDecimal("4.25")));
    }

    @Test
    void creaFacturaConTotalesCalculados() {
        FacturaDto creada = crear.ejecutar(nueva("F-001", dosLineas()));

        assertThat(creada.id()).isNotNull();
        assertThat(creada.lineas()).extracting(linea -> linea.importe().toPlainString())
                .containsExactly("21.00", "4.25");
        assertThat(creada.base()).isEqualByComparingTo("25.25");
        assertThat(creada.cuotaIva()).isEqualByComparingTo("5.30");
        assertThat(creada.total()).isEqualByComparingTo("30.55");
    }

    @Test
    void conservaLosDatosDeLaFacturaYSusLineas() {
        FacturaDto creada = crear.ejecutar(nueva("F-001", dosLineas()));

        assertThat(creada.numero()).isEqualTo("F-001");
        assertThat(creada.tipo()).isEqualTo("EMITIDA");
        assertThat(creada.fecha()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(creada.clienteNombre()).isEqualTo("Cliente SL");
        assertThat(creada.clienteNif()).isEqualTo("B12345678");
        assertThat(creada.porcentajeIva()).isEqualByComparingTo("21");
        assertThat(creada.lineas()).extracting(LineaFacturaDto::concepto).containsExactly("Primera", "Segunda");
        assertThat(creada.lineas().get(0).cantidad()).isEqualByComparingTo("2");
        assertThat(creada.lineas().get(0).precioUnitario()).isEqualByComparingTo("10.50");
        assertThat(obtener.ejecutar(creada.id())).isEqualTo(creada);
    }

    @Test
    void rechazaFacturaConLineaNulaOInvalidaSinGuardarla() {
        List<NuevaLineaFacturaDto> conNula = new ArrayList<>();
        conNula.add(null);
        List<NuevaLineaFacturaDto> conCantidadCero = List.of(
                new NuevaLineaFacturaDto("Servicio", BigDecimal.ZERO, BigDecimal.ONE));

        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", conNula)))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", conCantidadCero)))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void creaFacturaRecibidaConservandoSuTipo() {
        FacturaDto creada = crear.ejecutar(nueva("P-001", "RECIBIDA", dosLineas()));

        assertThat(creada.tipo()).isEqualTo("RECIBIDA");
        assertThat(obtener.ejecutar(creada.id()).tipo()).isEqualTo("RECIBIDA");
    }

    @Test
    void rechazaTipoNuloOInexistenteSinGuardarNada() {
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", null, dosLineas())))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El tipo de factura es obligatorio");
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", "ABONO", dosLineas())))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El tipo de factura debe ser EMITIDA o RECIBIDA");
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", "emitida", dosLineas())))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void conservaLaFechaDeVencimientoOpcional() {
        FacturaDto conVencimiento = crear.ejecutar(nueva("F-001", "EMITIDA", LocalDate.of(2026, 12, 3), dosLineas()));
        FacturaDto sinVencimiento = crear.ejecutar(nueva("F-002", dosLineas()));

        assertThat(obtener.ejecutar(conVencimiento.id()).fechaVencimiento()).isEqualTo(LocalDate.of(2026, 12, 3));
        assertThat(obtener.ejecutar(sinVencimiento.id()).fechaVencimiento()).isNull();
    }

    @Test
    void rechazaVencimientoAnteriorALaFechaSinGuardarNada() {
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", "EMITIDA", LocalDate.of(2026, 11, 2), dosLineas())))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La fecha de vencimiento no puede ser anterior a la fecha de la factura");
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void listaVaciaSinFacturas() {
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void eliminarSoloBorraLaFacturaIndicada() {
        Long primera = crear.ejecutar(nueva("F-001", dosLineas())).id();
        crear.ejecutar(nueva("F-002", dosLineas()));

        eliminar.ejecutar(primera);

        assertThat(listar.ejecutar()).extracting(FacturaDto::numero).containsExactly("F-002");
    }

    @Test
    void rechazaNumeroDuplicado() {
        crear.ejecutar(nueva("F-001", dosLineas()));

        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", dosLineas())))
                .isInstanceOf(NumeroFacturaDuplicadoException.class);
        assertThat(listar.ejecutar()).hasSize(1);
    }

    @Test
    void rechazaFacturaSinLineas() {
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", List.of())))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", null)))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void obtieneFacturaExistenteOFalla() {
        Long id = crear.ejecutar(nueva("F-001", dosLineas())).id();

        assertThat(obtener.ejecutar(id).numero()).isEqualTo("F-001");
        assertThatThrownBy(() -> obtener.ejecutar(99L)).isInstanceOf(FacturaNoEncontradaException.class);
    }

    @Test
    void listaFacturasEnOrdenDeCreacion() {
        crear.ejecutar(nueva("F-001", dosLineas()));
        crear.ejecutar(nueva("F-002", dosLineas()));

        assertThat(listar.ejecutar()).extracting(FacturaDto::numero).containsExactly("F-001", "F-002");
    }

    @Test
    void eliminaFacturaExistenteOFalla() {
        Long id = crear.ejecutar(nueva("F-001", dosLineas())).id();

        eliminar.ejecutar(id);

        assertThat(listar.ejecutar()).isEmpty();
        assertThatThrownBy(() -> eliminar.ejecutar(id)).isInstanceOf(FacturaNoEncontradaException.class);
    }

    @Test
    void buscarDevuelveLaFacturaExistenteConSusTotales() {
        FacturaDto creada = crear.ejecutar(nueva("F-001", dosLineas()));

        assertThat(buscar.ejecutar(creada.id())).contains(creada);
        assertThat(buscar.ejecutar(creada.id())).hasValueSatisfying(
                factura -> assertThat(factura.total()).isEqualByComparingTo("30.55"));
    }

    @Test
    void buscarFacturaInexistenteDevuelveVacioSinLanzar() {
        assertThat(buscar.ejecutar(99L)).isEmpty();
    }

    @Test
    void eliminarPublicaElEventoDeFacturaEliminadaConSuId() {
        Long id = crear.ejecutar(nueva("F-001", dosLineas())).id();

        eliminar.ejecutar(id);

        assertThat(eventos).containsExactly(new FacturaEliminadaEvento(id));
    }

    @Test
    void eliminarFacturaInexistenteNoPublicaNingunEvento() {
        assertThatThrownBy(() -> eliminar.ejecutar(99L)).isInstanceOf(FacturaNoEncontradaException.class);

        assertThat(eventos).isEmpty();
    }
}
