package com.facturacion.documentos.internal.application;

public record NuevoDocumentoDto(
        String entidadTipo,
        Long entidadId,
        String nombreArchivo,
        String contentType,
        byte[] contenido) {
}
