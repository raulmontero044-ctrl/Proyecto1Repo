package com.facturacion.documentos.api.http.handlers;

import com.facturacion.documentos.api.http.controllers.DocumentoController;
import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DocumentoController.class)
public class DocumentoNoEncontradoExceptionHandler {

    @ExceptionHandler(DocumentoNoEncontradoException.class)
    public ProblemDetail noEncontrado(DocumentoNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
