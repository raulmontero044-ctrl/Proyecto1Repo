package com.facturacion.ficticio.api.http.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class UnaExcepcionExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail estadoIlegal(IllegalStateException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
