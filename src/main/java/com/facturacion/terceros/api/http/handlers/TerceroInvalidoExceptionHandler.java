package com.facturacion.terceros.api.http.handlers;

import com.facturacion.terceros.api.http.controllers.TerceroController;
import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TerceroController.class)
public class TerceroInvalidoExceptionHandler {

    @ExceptionHandler(TerceroInvalidoException.class)
    public ProblemDetail invalido(TerceroInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
