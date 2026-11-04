package com.facturacion.ficticio.api.http.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ExcepcionesAgrupadasExceptionHandler {

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    ProblemDetail agrupadas(RuntimeException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
