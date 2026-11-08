package com.facturacion.documentos.internal.application;

import java.time.Instant;

public record DocumentoDto(
        Long id,
        String entidadTipo,
        Long entidadId,
        String nombre,
        String contentType,
        long tamano,
        String origen,
        Instant creadoEn) {
}
