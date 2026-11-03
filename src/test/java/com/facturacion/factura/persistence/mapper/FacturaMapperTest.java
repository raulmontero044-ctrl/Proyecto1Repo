package com.facturacion.factura.persistence.mapper;

import com.facturacion.factura.domain.model.Factura;
import com.facturacion.factura.domain.model.LineaFactura;
import com.facturacion.factura.persistence.entity.FacturaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FacturaMapperTest {

    private final FacturaMapper mapper = new FacturaMapper();

    @Test
    void conservaDatosYOrdenDeLineasEnIdaYVuelta() {
        Factura factura = new Factura(
                7L,
                "F-001",
                LocalDate.of(2026, 11, 3),
                "Cliente SL",
                "B12345678",
                new BigDecimal("21.00"),
                List.of(
                        new LineaFactura("Primera", new BigDecimal("2"), new BigDecimal("10.50")),
                        new LineaFactura("Segunda", new BigDecimal("1"), new BigDecimal("4.25"))));

        FacturaEntity entidad = mapper.aEntidad(factura);

        assertThat(entidad.getLineas()).extracting("posicion").containsExactly(0, 1);
        assertThat(mapper.aDominio(entidad)).isEqualTo(factura);
    }
}
