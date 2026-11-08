package com.facturacion.documentos.api.http.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DocumentoResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializaLaFechaDeCreacionEnIso8601YElTamanoComoNumero() throws Exception {
        DocumentoResponse respuesta = new DocumentoResponse(
                1L, "FACTURA", 10L, "factura-F-1.pdf", "application/pdf", 10_485_760L, "GENERADO",
                Instant.parse("2026-11-08T11:00:00.123Z"));

        String json = objectMapper.writeValueAsString(respuesta);

        assertThat(json).isEqualTo(
                "{\"id\":1,\"entidadTipo\":\"FACTURA\",\"entidadId\":10,\"nombre\":\"factura-F-1.pdf\","
                        + "\"contentType\":\"application/pdf\",\"tamano\":10485760,\"origen\":\"GENERADO\","
                        + "\"creadoEn\":\"2026-11-08T11:00:00.123Z\"}");
    }
}
