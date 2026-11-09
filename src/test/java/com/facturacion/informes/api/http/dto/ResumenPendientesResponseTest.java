package com.facturacion.informes.api.http.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ResumenPendientesResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializaLosImportesConSuEscalaDosYLasFechasEnIso() throws Exception {
        ResumenPendientesResponse respuesta = new ResumenPendientesResponse(
                new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("50.00"), new BigDecimal("0.00"),
                List.of(new SaldoFacturaResponse(
                        4L, "R-4", "RECIBIDA", LocalDate.of(2026, 3, 1), null, new BigDecimal("60.00"),
                        new BigDecimal("10.00"), new BigDecimal("50.00"), "PARCIAL", false)));

        String json = objectMapper.writeValueAsString(respuesta);

        assertThat(json).isEqualTo(
                "{\"pendienteCobro\":0.00,\"vencidoCobro\":0.00,\"pendientePago\":50.00,\"vencidoPago\":0.00,"
                        + "\"facturas\":[{\"facturaId\":4,\"numero\":\"R-4\",\"tipo\":\"RECIBIDA\","
                        + "\"fecha\":\"2026-03-01\",\"fechaVencimiento\":null,\"total\":60.00,\"pagado\":10.00,"
                        + "\"pendiente\":50.00,\"estado\":\"PARCIAL\",\"vencida\":false}]}");
    }
}
