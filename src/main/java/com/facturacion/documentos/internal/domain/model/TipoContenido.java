package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;

import java.util.Arrays;

public enum TipoContenido {
    PDF("application/pdf"),
    PNG("image/png"),
    JPEG("image/jpeg");

    private final String mime;

    TipoContenido(String mime) {
        this.mime = mime;
    }

    public String mime() {
        return mime;
    }

    public static TipoContenido desdeMime(String mime) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.mime.equalsIgnoreCase(mime == null ? "" : mime.strip()))
                .findFirst()
                .orElseThrow(() -> new DocumentoInvalidoException(
                        "El tipo de contenido del documento debe ser application/pdf, image/png o image/jpeg"));
    }
}
