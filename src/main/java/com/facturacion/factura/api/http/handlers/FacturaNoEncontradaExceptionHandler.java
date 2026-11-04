package com.facturacion.factura.api.http.handlers;

import com.facturacion.factura.api.http.controllers.FacturaController;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FacturaController.class)
public class FacturaNoEncontradaExceptionHandler {

    @ExceptionHandler(FacturaNoEncontradaException.class)
    public ProblemDetail noEncontrada(FacturaNoEncontradaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
