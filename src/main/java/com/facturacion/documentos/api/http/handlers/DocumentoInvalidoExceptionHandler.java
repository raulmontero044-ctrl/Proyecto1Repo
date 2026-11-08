package com.facturacion.documentos.api.http.handlers;

import com.facturacion.documentos.api.http.controllers.DocumentoController;
import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DocumentoController.class)
public class DocumentoInvalidoExceptionHandler {

    @ExceptionHandler(DocumentoInvalidoException.class)
    public ProblemDetail invalido(DocumentoInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
