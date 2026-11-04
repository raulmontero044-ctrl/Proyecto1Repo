package com.facturacion.factura.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    private static LineaFactura linea(String cantidad, String precio) {
        return new LineaFactura("Servicio", new BigDecimal(cantidad), new BigDecimal(precio));
    }

    private static Factura factura(String iva, List<LineaFactura> lineas) {
        return Factura.nueva("F-001", FECHA, "Cliente SL", "B12345678", new BigDecimal(iva), lineas);
    }

    @Test
    void nuevaFacturaNoTieneId() {
        assertThat(factura("21", List.of(linea("1", "1"))).id()).isNull();
    }

    @Test
    void copiaLasLineasDeFormaInmutable() {
        List<LineaFactura> lineas = new ArrayList<>(List.of(linea("1", "1")));
        Factura factura = factura("21", lineas);

        lineas.add(linea("1", "5"));

        assertThat(factura.lineas()).hasSize(1);
        assertThatThrownBy(() -> factura.lineas().add(linea("1", "1")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rechazaFacturaSinLineasOConLineaNula() {
        List<LineaFactura> conNula = new ArrayList<>();
        conNula.add(null);

        assertThatThrownBy(() -> factura("21", List.of())).isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> factura("21", null)).isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> factura("21", conNula)).isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaDatosObligatoriosVacios() {
        List<LineaFactura> lineas = List.of(linea("1", "1"));

        assertThatThrownBy(() -> Factura.nueva(" ", FECHA, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> Factura.nueva("F-1", null, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> Factura.nueva("F-1", FECHA, "", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> Factura.nueva("F-1", FECHA, "Cliente", null, BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaNumeroYClienteNulosYNifEnBlanco() {
        List<LineaFactura> lineas = List.of(linea("1", "1"));

        assertThatThrownBy(() -> Factura.nueva(null, FECHA, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El número de factura es obligatorio");
        assertThatThrownBy(() -> Factura.nueva("F-1", FECHA, null, "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El nombre del cliente es obligatorio");
        assertThatThrownBy(() -> Factura.nueva("F-1", FECHA, "Cliente", "  ", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El NIF del cliente es obligatorio");
    }

    @Test
    void informaElMotivoDeCadaInvarianteIncumplida() {
        List<LineaFactura> conNula = new ArrayList<>();
        conNula.add(null);

        assertThatThrownBy(() -> Factura.nueva("F-1", null, "Cliente", "NIF", BigDecimal.TEN, List.of(linea("1", "1"))))
                .hasMessage("La fecha de la factura es obligatoria");
        assertThatThrownBy(() -> factura("-0.01", List.of(linea("1", "1"))))
                .hasMessage("El porcentaje de IVA no puede ser negativo");
        assertThatThrownBy(() -> factura("21", List.of()))
                .hasMessage("La factura debe tener al menos una línea");
        assertThatThrownBy(() -> factura("21", conNula))
                .hasMessage("La factura no puede contener líneas vacías");
        assertThatThrownBy(() -> new LineaFactura("", BigDecimal.ONE, BigDecimal.ONE))
                .hasMessage("El concepto de la línea es obligatorio");
        assertThatThrownBy(() -> linea("0", "1"))
                .hasMessage("La cantidad de la línea debe ser mayor que cero");
        assertThatThrownBy(() -> linea("1", "-0.01"))
                .hasMessage("El precio unitario de la línea no puede ser negativo");
    }

    @Test
    void rechazaCantidadNegativa() {
        assertThatThrownBy(() -> linea("-1", "1")).isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void aceptaValoresEnElLimiteDeLasInvariantes() {
        LineaFactura linea = linea("0.001", "0");

        Factura factura = factura("0", List.of(linea));

        assertThat(factura.porcentajeIva()).isEqualByComparingTo("0");
        assertThat(factura.lineas()).containsExactly(linea);
        assertThat(linea.cantidad()).isEqualByComparingTo("0.001");
        assertThat(linea.precioUnitario()).isEqualByComparingTo("0");
    }

    @Test
    void rechazaIvaNegativo() {
        assertThatThrownBy(() -> factura("-1", List.of(linea("1", "1"))))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaIvaNulo() {
        assertThatThrownBy(() -> Factura.nueva("F-1", FECHA, "Cliente", "NIF", null, List.of(linea("1", "1"))))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaDatosObligatoriosNulosDeLinea() {
        assertThatThrownBy(() -> new LineaFactura(null, BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> new LineaFactura("Servicio", null, BigDecimal.ONE))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> new LineaFactura("Servicio", BigDecimal.ONE, null))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaLineasInvalidas() {
        assertThatThrownBy(() -> linea("0", "1")).isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> linea("1", "-0.01")).isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> new LineaFactura(" ", BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(FacturaInvalidaException.class);
    }
}
