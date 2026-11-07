package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaLineaFacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaFacadeImplTest {

    private final FacturaRepositoryEnMemoria repository = new FacturaRepositoryEnMemoria();
    private final FacturaFacade facade = new FacturaFacadeImpl(
            new CrearFactura(repository),
            new ObtenerFactura(repository),
            new ListarFacturas(repository),
            new EliminarFactura(repository, evento -> { }),
            new BuscarFactura(repository));

    private static NuevaFacturaDto nueva(String numero, List<NuevaLineaFacturaDto> lineas) {
        return new NuevaFacturaDto(
                numero,
                LocalDate.of(2026, 11, 3),
                "Cliente SL",
                "B12345678",
                new BigDecimal("21"),
                lineas);
    }

    private static NuevaFacturaDto nueva(String numero) {
        return nueva(numero, List.of(
                new NuevaLineaFacturaDto("Primera", new BigDecimal("2"), new BigDecimal("10.50")),
                new NuevaLineaFacturaDto("Segunda", new BigDecimal("1"), new BigDecimal("4.25"))));
    }

    @Test
    void crearDevuelveLaFacturaConTotalesCalculadosYLaPersiste() {
        FacturaDto creada = facade.crear(nueva("F-001"));

        assertThat(creada.id()).isNotNull();
        assertThat(creada.numero()).isEqualTo("F-001");
        assertThat(creada.lineas()).extracting(linea -> linea.importe().toPlainString())
                .containsExactly("21.00", "4.25");
        assertThat(creada.base()).isEqualByComparingTo("25.25");
        assertThat(creada.cuotaIva()).isEqualByComparingTo("5.30");
        assertThat(creada.total()).isEqualByComparingTo("30.55");
        assertThat(repository.buscarPorId(creada.id())).isPresent();
    }

    @Test
    void crearRechazaNumeroDuplicadoSinGuardarOtraFactura() {
        facade.crear(nueva("F-001"));

        assertThatThrownBy(() -> facade.crear(nueva("F-001")))
                .isInstanceOf(NumeroFacturaDuplicadoException.class)
                .hasMessage("Ya existe una factura con el número F-001");
        assertThat(facade.listar()).hasSize(1);
    }

    @Test
    void crearRechazaFacturaSinLineasSinGuardarNada() {
        assertThatThrownBy(() -> facade.crear(nueva("F-001", List.of())))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThat(facade.listar()).isEmpty();
    }

    @Test
    void obtenerDevuelveLaMismaFacturaQueSeCreo() {
        FacturaDto creada = facade.crear(nueva("F-001"));

        assertThat(facade.obtener(creada.id())).isEqualTo(creada);
    }

    @Test
    void obtenerFacturaInexistenteLanzaNoEncontrada() {
        assertThatThrownBy(() -> facade.obtener(99L))
                .isInstanceOf(FacturaNoEncontradaException.class)
                .hasMessage("No existe la factura con id 99");
    }

    @Test
    void buscarDevuelveLaMismaFacturaQueSeCreo() {
        FacturaDto creada = facade.crear(nueva("F-001"));

        assertThat(facade.buscar(creada.id())).contains(creada);
    }

    @Test
    void buscarFacturaInexistenteDevuelveVacio() {
        assertThat(facade.buscar(99L)).isEmpty();
    }

    @Test
    void listarSinFacturasDevuelveListaVacia() {
        assertThat(facade.listar()).isEmpty();
    }

    @Test
    void listarDevuelveLasFacturasEnOrdenDeCreacion() {
        facade.crear(nueva("F-001"));
        facade.crear(nueva("F-002"));

        assertThat(facade.listar()).extracting(FacturaDto::numero).containsExactly("F-001", "F-002");
    }

    @Test
    void eliminarSoloBorraLaFacturaIndicada() {
        Long primera = facade.crear(nueva("F-001")).id();
        facade.crear(nueva("F-002"));

        facade.eliminar(primera);

        assertThat(facade.listar()).extracting(FacturaDto::numero).containsExactly("F-002");
    }

    @Test
    void eliminarFacturaInexistenteLanzaNoEncontradaSinBorrarNada() {
        facade.crear(nueva("F-001"));

        assertThatThrownBy(() -> facade.eliminar(99L))
                .isInstanceOf(FacturaNoEncontradaException.class);
        assertThat(facade.listar()).hasSize(1);
    }
}
