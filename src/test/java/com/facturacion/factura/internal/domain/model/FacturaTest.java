package com.facturacion.factura.internal.domain.model;

import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);
    private static final String DIRECCION_EMPRESA = "Calle Mayor 1, 28001, Madrid, Madrid, España";

    private static LineaFactura linea(String cantidad, String precio) {
        return new LineaFactura("Servicio", new BigDecimal(cantidad), new BigDecimal(precio));
    }

    private static Factura completa(
            TipoFactura tipo,
            LocalDate fechaVencimiento,
            Long empresaId,
            String empresaRazonSocial,
            String empresaNif,
            String empresaDireccion,
            Long terceroId,
            String terceroDireccion) {
        return Factura.nueva(
                "F-1", tipo, FECHA, fechaVencimiento,
                empresaId, empresaRazonSocial, empresaNif, empresaDireccion,
                terceroId, "Cliente", "NIF", terceroDireccion,
                BigDecimal.TEN, List.of(linea("1", "1")));
    }

    private static Factura conEmpresa(Long empresaId, String razonSocial, String nif, String direccion) {
        return completa(TipoFactura.EMITIDA, null, empresaId, razonSocial, nif, direccion, 10L, null);
    }

    private static Factura nueva(
            String numero, LocalDate fecha, String nombre, String nif, BigDecimal iva, List<LineaFactura> lineas) {
        return Factura.nueva(
                numero, TipoFactura.EMITIDA, fecha, null,
                1L, "Empresa SL", "B00000001", DIRECCION_EMPRESA,
                10L, nombre, nif, null,
                iva, lineas);
    }

    private static Factura factura(String iva, List<LineaFactura> lineas) {
        return nueva("F-001", FECHA, "Cliente SL", "B12345678", new BigDecimal(iva), lineas);
    }

    @Test
    void conservaElTipoDeLaFactura() {
        Factura recibida = completa(TipoFactura.RECIBIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, null);

        assertThat(recibida.tipo()).isEqualTo(TipoFactura.RECIBIDA);
    }

    private static Factura conVencimiento(LocalDate fechaVencimiento) {
        return completa(TipoFactura.EMITIDA, fechaVencimiento, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, null);
    }

    @Test
    void aceptaVencimientoAusenteIgualOPosteriorALaFecha() {
        assertThat(conVencimiento(null).fechaVencimiento()).isNull();
        assertThat(conVencimiento(FECHA).fechaVencimiento()).isEqualTo(FECHA);
        assertThat(conVencimiento(FECHA.plusDays(30)).fechaVencimiento()).isEqualTo(LocalDate.of(2026, 12, 3));
    }

    @Test
    void rechazaVencimientoAnteriorALaFecha() {
        assertThatThrownBy(() -> conVencimiento(FECHA.minusDays(1)))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La fecha de vencimiento no puede ser anterior a la fecha de la factura");
    }

    @Test
    void nuevaExigeEmpresaYTercero() {
        assertThatThrownBy(() -> completa(TipoFactura.EMITIDA, null, null, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, null))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La empresa de la factura es obligatoria");
        assertThatThrownBy(() -> completa(TipoFactura.EMITIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, null, null))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El tercero de la factura es obligatorio");
    }

    @Test
    void nuevaExigeLaFotografiaDeLaEmpresa() {
        assertThatThrownBy(() -> conEmpresa(1L, null, "B1", DIRECCION_EMPRESA))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La razón social de la empresa es obligatoria");
        assertThatThrownBy(() -> conEmpresa(1L, "Empresa SL", " ", DIRECCION_EMPRESA))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El NIF de la empresa es obligatorio");
        assertThatThrownBy(() -> conEmpresa(1L, "Empresa SL", "B1", ""))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La dirección de la empresa es obligatoria");
        assertThatThrownBy(() -> conEmpresa(1L, "Empresa SL", "B1", null))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La dirección de la empresa es obligatoria");
    }

    @Test
    void conservaLaFotografiaDeEmpresaYTerceroYAdmiteTerceroSinDireccion() {
        Factura conDireccion = completa(
                TipoFactura.EMITIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, "Avenida 2, 08001, Barcelona");
        Factura sinDireccion = completa(TipoFactura.EMITIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, null);

        assertThat(conDireccion.empresaRazonSocial()).isEqualTo("Empresa SL");
        assertThat(conDireccion.empresaNif()).isEqualTo("B1");
        assertThat(conDireccion.empresaDireccion()).isEqualTo(DIRECCION_EMPRESA);
        assertThat(conDireccion.terceroNombre()).isEqualTo("Cliente");
        assertThat(conDireccion.terceroNif()).isEqualTo("NIF");
        assertThat(conDireccion.terceroDireccion()).isEqualTo("Avenida 2, 08001, Barcelona");
        assertThat(sinDireccion.terceroDireccion()).isNull();
    }

    @Test
    void limitaLasDireccionesDeLaFotografiaA600Caracteres() {
        assertThat(conEmpresa(1L, "Empresa SL", "B1", "D".repeat(600)).empresaDireccion()).hasSize(600);
        assertThat(completa(TipoFactura.EMITIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, "T".repeat(600))
                .terceroDireccion()).hasSize(600);
        assertThatThrownBy(() -> conEmpresa(1L, "Empresa SL", "B1", "D".repeat(601)))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La dirección de la empresa no puede superar 600 caracteres");
        assertThatThrownBy(() -> completa(
                TipoFactura.EMITIDA, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, "T".repeat(601)))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("La dirección del tercero no puede superar 600 caracteres");
    }

    @Test
    void laReconstruccionToleraFilasAnterioresSinEmpresaNiTerceroNiFotografia() {
        Factura anterior = new Factura(
                3L, "F-3", TipoFactura.EMITIDA, FECHA, null,
                null, null, null, null,
                null, "Cliente", "NIF", null,
                BigDecimal.TEN, List.of(linea("1", "1")));

        assertThat(anterior.empresaId()).isNull();
        assertThat(anterior.empresaRazonSocial()).isNull();
        assertThat(anterior.empresaNif()).isNull();
        assertThat(anterior.empresaDireccion()).isNull();
        assertThat(anterior.terceroId()).isNull();
        assertThat(anterior.terceroDireccion()).isNull();
    }

    @Test
    void rechazaFacturaSinTipo() {
        assertThatThrownBy(() -> completa(null, null, 1L, "Empresa SL", "B1", DIRECCION_EMPRESA, 10L, null))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El tipo de factura es obligatorio");
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

        assertThatThrownBy(() -> nueva(" ", FECHA, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> nueva("F-1", null, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> nueva("F-1", FECHA, "", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> nueva("F-1", FECHA, "Cliente", null, BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void rechazaNumeroYClienteNulosYNifEnBlanco() {
        List<LineaFactura> lineas = List.of(linea("1", "1"));

        assertThatThrownBy(() -> nueva(null, FECHA, "Cliente", "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El número de factura es obligatorio");
        assertThatThrownBy(() -> nueva("F-1", FECHA, null, "NIF", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El nombre del tercero es obligatorio");
        assertThatThrownBy(() -> nueva("F-1", FECHA, "Cliente", "  ", BigDecimal.TEN, lineas))
                .isInstanceOf(FacturaInvalidaException.class)
                .hasMessage("El NIF del tercero es obligatorio");
    }

    @Test
    void informaElMotivoDeCadaInvarianteIncumplida() {
        List<LineaFactura> conNula = new ArrayList<>();
        conNula.add(null);

        assertThatThrownBy(() -> nueva("F-1", null, "Cliente", "NIF", BigDecimal.TEN, List.of(linea("1", "1"))))
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
        assertThatThrownBy(() -> nueva("F-1", FECHA, "Cliente", "NIF", null, List.of(linea("1", "1"))))
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
