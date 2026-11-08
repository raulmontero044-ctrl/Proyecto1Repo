package com.facturacion.factura.internal.persistence.mapper;

import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.LineaFactura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.persistence.entity.FacturaEntity;
import com.facturacion.factura.internal.persistence.entity.LineaFacturaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacturaMapperTest {

    private final FacturaMapper mapper = new FacturaMapper();

    private static FacturaEntity entidadValida(Long id, String numero) {
        return new FacturaEntity(
                id, numero, "EMITIDA", LocalDate.of(2026, 11, 3), null, 1L, "Empresa SL", "B00000001", "Calle Mayor 1", 10L, "Cliente SL", "B12345678", null, new BigDecimal("21.00"));
    }

    @Test
    void conservaDatosYOrdenDeLineasEnIdaYVuelta() {
        Factura factura = new Factura(
                7L,
                "F-001",
                TipoFactura.RECIBIDA,
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 12, 3),
                1L,
                "Empresa SL",
                "B00000001",
                "Calle Mayor 1, 28001, Madrid, Madrid, España",
                10L,
                "Cliente SL",
                "B12345678",
                "Avenida Diagonal 2, 08001, Barcelona, Barcelona, España",
                new BigDecimal("21.00"),
                List.of(
                        new LineaFactura("Primera", new BigDecimal("2"), new BigDecimal("10.50")),
                        new LineaFactura("Segunda", new BigDecimal("1"), new BigDecimal("4.25"))));

        FacturaEntity entidad = mapper.aEntidad(factura);

        assertThat(entidad.getTipo()).isEqualTo("RECIBIDA");
        assertThat(entidad.getFechaVencimiento()).isEqualTo(LocalDate.of(2026, 12, 3));
        assertThat(entidad.getLineas()).extracting("posicion").containsExactly(0, 1);
        assertThat(mapper.aDominio(entidad)).isEqualTo(factura);
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        FacturaEntity sinNumero = entidadValida(3L, " ");
        sinNumero.agregarLinea(new LineaFacturaEntity(0, "Servicio", BigDecimal.ONE, BigDecimal.ONE));
        FacturaEntity sinLineas = entidadValida(4L, "F-004");

        assertThatThrownBy(() -> mapper.aDominio(sinNumero)).isInstanceOf(FacturaInvalidaException.class);
        assertThatThrownBy(() -> mapper.aDominio(sinLineas)).isInstanceOf(FacturaInvalidaException.class);
    }

    @Test
    void aDominioConservaElOrdenDeLasLineasDeLaEntidadSinExponerLaPosicion() {
        FacturaEntity entidad = entidadValida(8L, "F-008");
        entidad.agregarLinea(new LineaFacturaEntity(0, "Primera", BigDecimal.ONE, BigDecimal.TEN));
        entidad.agregarLinea(new LineaFacturaEntity(1, "Segunda", new BigDecimal("2"), BigDecimal.ONE));

        Factura factura = mapper.aDominio(entidad);

        assertThat(factura.id()).isEqualTo(8L);
        assertThat(factura.lineas()).containsExactly(
                new LineaFactura("Primera", BigDecimal.ONE, BigDecimal.TEN),
                new LineaFactura("Segunda", new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    void aDominioToleraLosNulosDeLasFilasAnterioresALaAmpliacion() {
        FacturaEntity anterior = new FacturaEntity(
                9L, "F-009", "EMITIDA", LocalDate.of(2026, 11, 3), null,
                null, null, null, null,
                null, "Cliente SL", "B12345678", null,
                new BigDecimal("21.00"));
        anterior.agregarLinea(new LineaFacturaEntity(0, "Servicio", BigDecimal.ONE, BigDecimal.ONE));

        Factura factura = mapper.aDominio(anterior);

        assertThat(factura.tipo()).isEqualTo(TipoFactura.EMITIDA);
        assertThat(factura.fechaVencimiento()).isNull();
        assertThat(factura.empresaId()).isNull();
        assertThat(factura.empresaRazonSocial()).isNull();
        assertThat(factura.empresaNif()).isNull();
        assertThat(factura.empresaDireccion()).isNull();
        assertThat(factura.terceroId()).isNull();
        assertThat(factura.terceroNombre()).isEqualTo("Cliente SL");
        assertThat(factura.terceroNif()).isEqualTo("B12345678");
        assertThat(factura.terceroDireccion()).isNull();
    }

    @Test
    void facturaNuevaGeneraEntidadSinIdYConDatosCopiados() {
        Factura nueva = Factura.nueva(
                "F-002",
                TipoFactura.EMITIDA,
                LocalDate.of(2026, 11, 3),
                null,
                1L,
                "Empresa SL",
                "B00000001",
                "Calle Mayor 1, 28001, Madrid, Madrid, España",
                10L,
                "Cliente SL",
                "B12345678",
                "Avenida Diagonal 2, 08001, Barcelona, Barcelona, España",
                new BigDecimal("10.50"),
                List.of(new LineaFactura("Única", new BigDecimal("1.5"), new BigDecimal("3.00"))));

        FacturaEntity entidad = mapper.aEntidad(nueva);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getNumero()).isEqualTo("F-002");
        assertThat(entidad.getTipo()).isEqualTo("EMITIDA");
        assertThat(entidad.getFecha()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(entidad.getFechaVencimiento()).isNull();
        assertThat(entidad.getEmpresaId()).isEqualTo(1L);
        assertThat(entidad.getEmpresaRazonSocial()).isEqualTo("Empresa SL");
        assertThat(entidad.getEmpresaNif()).isEqualTo("B00000001");
        assertThat(entidad.getEmpresaDireccion()).isEqualTo("Calle Mayor 1, 28001, Madrid, Madrid, España");
        assertThat(entidad.getTerceroId()).isEqualTo(10L);
        assertThat(entidad.getTerceroDireccion()).isEqualTo("Avenida Diagonal 2, 08001, Barcelona, Barcelona, España");
        assertThat(entidad.getTerceroNombre()).isEqualTo("Cliente SL");
        assertThat(entidad.getTerceroNif()).isEqualTo("B12345678");
        assertThat(entidad.getPorcentajeIva()).isEqualByComparingTo("10.50");
        assertThat(entidad.getLineas()).singleElement().satisfies(linea -> {
            assertThat(linea.getConcepto()).isEqualTo("Única");
            assertThat(linea.getCantidad()).isEqualByComparingTo("1.5");
            assertThat(linea.getPrecioUnitario()).isEqualByComparingTo("3.00");
        });
    }
}
