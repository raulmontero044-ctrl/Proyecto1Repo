package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
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
    private final EliminarFactura eliminar = new EliminarFactura(repository);

    private static NuevaFacturaDto nueva(String numero, List<NuevaLineaFacturaDto> lineas) {
        return new NuevaFacturaDto(
                numero,
                LocalDate.of(2026, 11, 3),
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
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La factura no puede contener líneas vacías");
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", conCantidadCero)))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La cantidad de la línea debe ser mayor que cero");
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
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La factura debe tener al menos una línea");
        assertThatThrownBy(() -> crear.ejecutar(nueva("F-001", null)))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La factura debe tener al menos una línea");
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
    void eliminarFacturaInexistenteIndicaElIdNoEncontrado() {
        crear.ejecutar(nueva("F-001", dosLineas()));

        assertThatThrownBy(() -> eliminar.ejecutar(99L))
                .isInstanceOf(FacturaNoEncontradaException.class)
                .hasMessage("No existe la factura con id 99");
    }

    @Test
    void listarDevuelveUnaListaQueNoSePuedeModificar() {
        crear.ejecutar(nueva("F-001", dosLineas()));

        List<FacturaDto> facturas = listar.ejecutar();

        assertThatThrownBy(facturas::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void laFacturaDevueltaTieneLineasQueNoSePuedenModificar() {
        FacturaDto creada = crear.ejecutar(nueva("F-001", dosLineas()));

        assertThatThrownBy(() -> creada.lineas().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
