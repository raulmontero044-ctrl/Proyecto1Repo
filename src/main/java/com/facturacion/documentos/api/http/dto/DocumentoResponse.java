package com.facturacion.documentos.api.http.dto;

import java.time.Instant;

public record DocumentoResponse(
        Long id,
        String entidadTipo,
        Long entidadId,
        String nombre,
        String contentType,
        long tamano,
        String origen,
        Instant creadoEn) {
}
