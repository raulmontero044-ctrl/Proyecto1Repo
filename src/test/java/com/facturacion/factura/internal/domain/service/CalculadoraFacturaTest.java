package com.facturacion.factura.internal.domain.service;

import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.LineaFactura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraFacturaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    private static LineaFactura linea(String cantidad, String precio) {
        return new LineaFactura("Servicio", new BigDecimal(cantidad), new BigDecimal(precio));
    }

    private static Factura factura(String iva, List<LineaFactura> lineas) {
        return Factura.nueva("F-001", TipoFactura.EMITIDA, FECHA, null, "Cliente SL", "B12345678", new BigDecimal(iva), lineas);
    }

    @Test
    void calculaBaseCuotaIvaYTotal() {
        Factura factura = factura("21", List.of(linea("2", "10.50"), linea("1", "4.25")));

        assertThat(CalculadoraFactura.base(factura)).isEqualByComparingTo("25.25");
        assertThat(CalculadoraFactura.cuotaIva(factura)).isEqualByComparingTo("5.30");
        assertThat(CalculadoraFactura.total(factura)).isEqualByComparingTo("30.55");
        assertThat(CalculadoraFactura.total(factura).scale()).isEqualTo(2);
    }

    @Test
    void redondeaImporteDeLineaHalfUp() {
        assertThat(CalculadoraFactura.importe(linea("3", "0.335"))).isEqualByComparingTo("1.01");
    }

    @Test
    void calculaImporteConCantidadDecimal() {
        assertThat(CalculadoraFactura.importe(linea("1.333", "3.00"))).isEqualByComparingTo("4.00");
        assertThat(CalculadoraFactura.importe(linea("0.001", "9999999999.99"))).isEqualByComparingTo("10000000.00");
    }

    @Test
    void sumaLosImportesYaRedondeadosDeCadaLinea() {
        Factura factura = factura("0", List.of(linea("1", "0.335"), linea("1", "0.335")));

        assertThat(CalculadoraFactura.base(factura)).isEqualByComparingTo("0.68");
    }

    @Test
    void redondeaCuotaIvaHalfUp() {
        Factura factura = factura("21", List.of(linea("1", "2.50")));

        assertThat(CalculadoraFactura.cuotaIva(factura)).isEqualByComparingTo("0.53");
        assertThat(CalculadoraFactura.total(factura)).isEqualByComparingTo("3.03");
    }

    @Test
    void calculaConPorcentajeIvaDecimal() {
        Factura factura = factura("10.5", List.of(linea("2", "10.50"), linea("1", "4.25")));

        assertThat(CalculadoraFactura.cuotaIva(factura)).isEqualByComparingTo("2.65");
        assertThat(CalculadoraFactura.total(factura)).isEqualByComparingTo("27.90");
    }

    @Test
    void devuelveSiempreDosDecimales() {
        Factura factura = factura("21", List.of(linea("1", "10")));

        assertThat(CalculadoraFactura.importe(factura.lineas().get(0)).scale()).isEqualTo(2);
        assertThat(CalculadoraFactura.base(factura).scale()).isEqualTo(2);
        assertThat(CalculadoraFactura.cuotaIva(factura).scale()).isEqualTo(2);
    }

    @Test
    void permiteIvaCeroYPrecioCero() {
        Factura factura = factura("0", List.of(linea("1", "0")));

        assertThat(CalculadoraFactura.total(factura)).isEqualByComparingTo("0.00");
    }
}
