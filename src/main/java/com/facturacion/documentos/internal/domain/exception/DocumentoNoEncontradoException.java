package com.facturacion.documentos.internal.domain.exception;

public class DocumentoNoEncontradoException extends RuntimeException {

    public DocumentoNoEncontradoException(Long id) {
        super("No existe el documento con id " + id);
    }
}
