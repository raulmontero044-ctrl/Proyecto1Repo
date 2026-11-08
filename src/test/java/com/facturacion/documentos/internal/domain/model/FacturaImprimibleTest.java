package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaImprimibleTest {

    private static final Interviniente EMPRESA = new Interviniente("Empresa SL", "B00000001", "Calle Mayor 1");
    private static final Interviniente TERCERO = new Interviniente("Tercero SA", "A00000020", "Plaza 2");

    private static FacturaImprimible factura(TipoFactura tipo, List<LineaImprimible> lineas) {
        return new FacturaImprimible(
                "F-1", tipo, LocalDate.of(2026, 11, 3), null, EMPRESA, TERCERO, new BigDecimal("21.00"),
                lineas, new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"));
    }

    @Test
    void enUnaFacturaEmitidaElEmisorEsLaEmpresaYElReceptorElTercero() {
        FacturaImprimible factura = factura(TipoFactura.EMITIDA, List.of());

        assertThat(factura.emisor()).isEqualTo(EMPRESA);
        assertThat(factura.receptor()).isEqualTo(TERCERO);
    }

    @Test
    void enUnaFacturaRecibidaElEmisorEsElTerceroYElReceptorLaEmpresa() {
        FacturaImprimible factura = factura(TipoFactura.RECIBIDA, List.of());

        assertThat(factura.emisor()).isEqualTo(TERCERO);
        assertThat(factura.receptor()).isEqualTo(EMPRESA);
    }

    @Test
    void copiaLasLineasDeFormaInmutable() {
        List<LineaImprimible> lineas = new ArrayList<>();
        lineas.add(new LineaImprimible("Servicio", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN));

        FacturaImprimible factura = factura(TipoFactura.EMITIDA, lineas);
        lineas.clear();

        assertThat(factura.lineas()).hasSize(1);
        assertThatThrownBy(() -> factura.lineas().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unaFacturaSinLineasTieneUnaListaVacia() {
        assertThat(factura(TipoFactura.EMITIDA, null).lineas()).isEmpty();
    }

    @Test
    void rechazaUnaFacturaSinTipo() {
        assertThatThrownBy(() -> factura(null, List.of()))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
    }

    @Test
    void rechazaUnaFacturaSinEmpresaOSinTercero() {
        assertThatThrownBy(() -> new FacturaImprimible(
                "F-1", TipoFactura.EMITIDA, null, null, null, TERCERO, null, List.of(), null, null, null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La factura debe tener empresa y tercero");
        assertThatThrownBy(() -> new FacturaImprimible(
                "F-1", TipoFactura.EMITIDA, null, null, EMPRESA, null, null, List.of(), null, null, null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("La factura debe tener empresa y tercero");
    }

    @Test
    void reconoceLosTiposDeFactura() {
        assertThat(TipoFactura.desde("EMITIDA")).isEqualTo(TipoFactura.EMITIDA);
        assertThat(TipoFactura.desde("RECIBIDA")).isEqualTo(TipoFactura.RECIBIDA);
    }

    @Test
    void rechazaUnTipoDeFacturaAusente() {
        assertThatThrownBy(() -> TipoFactura.desde(null))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de la factura es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "emitida", "PROFORMA"})
    void rechazaUnTipoDeFacturaDesconocido(String tipo) {
        assertThatThrownBy(() -> TipoFactura.desde(tipo))
                .isInstanceOf(DocumentoInvalidoException.class)
                .hasMessage("El tipo de la factura debe ser EMITIDA o RECIBIDA");
    }
}
