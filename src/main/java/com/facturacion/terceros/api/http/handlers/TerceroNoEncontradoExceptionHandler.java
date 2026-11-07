package com.facturacion.terceros.api.http.handlers;

import com.facturacion.terceros.api.http.controllers.TerceroController;
import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TerceroController.class)
public class TerceroNoEncontradoExceptionHandler {

    @ExceptionHandler(TerceroNoEncontradoException.class)
    public ProblemDetail noEncontrado(TerceroNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
