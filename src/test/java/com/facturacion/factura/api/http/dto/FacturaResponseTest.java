package com.facturacion.factura.api.http.dto;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class FacturaResponseTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializaExactamenteIgualQueElDtoDeContratoQueSeDevolviaAntes() throws Exception {
        FacturaDto dto = new FacturaDto(
                3L,
                "F-003",
                "EMITIDA",
                FECHA,
                null,
                "Cliente SL",
                "B12345678",
                new BigDecimal("21.00"),
                List.of(new LineaFacturaDto("Servicio", new BigDecimal("2.000"), new BigDecimal("10.50"), new BigDecimal("21.00"))),
                new BigDecimal("21.00"),
                new BigDecimal("4.41"),
                new BigDecimal("25.41"));
        FacturaResponse respuesta = new FacturaResponse(
                3L,
                "F-003",
                "EMITIDA",
                FECHA,
                null,
                "Cliente SL",
                "B12345678",
                new BigDecimal("21.00"),
                List.of(new LineaFacturaResponse("Servicio", new BigDecimal("2.000"), new BigDecimal("10.50"), new BigDecimal("21.00"))),
                new BigDecimal("21.00"),
                new BigDecimal("4.41"),
                new BigDecimal("25.41"));

        String json = objectMapper.writeValueAsString(respuesta);

        assertThat(json).isEqualTo(objectMapper.writeValueAsString(dto));
        assertThat(json).isEqualTo("{\"id\":3,\"numero\":\"F-003\",\"tipo\":\"EMITIDA\",\"fecha\":\"2026-11-03\",\"fechaVencimiento\":null,"
                + "\"clienteNombre\":\"Cliente SL\",\"clienteNif\":\"B12345678\",\"porcentajeIva\":21.00,"
                + "\"lineas\":[{\"concepto\":\"Servicio\",\"cantidad\":2.000,\"precioUnitario\":10.50,\"importe\":21.00}],"
                + "\"base\":21.00,\"cuotaIva\":4.41,\"total\":25.41}");
    }
}
