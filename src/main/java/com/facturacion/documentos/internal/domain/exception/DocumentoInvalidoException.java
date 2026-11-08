package com.facturacion.documentos.internal.domain.exception;

public class DocumentoInvalidoException extends RuntimeException {

    public DocumentoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
