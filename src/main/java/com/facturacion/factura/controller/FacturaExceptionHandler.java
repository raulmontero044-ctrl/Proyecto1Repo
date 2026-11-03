package com.facturacion.factura.controller;

import com.facturacion.factura.domain.model.FacturaInvalidaException;
import com.facturacion.factura.domain.model.FacturaNoEncontradaException;
import com.facturacion.factura.domain.model.NumeroFacturaDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FacturaController.class)
public class FacturaExceptionHandler {

    @ExceptionHandler(FacturaNoEncontradaException.class)
    public ProblemDetail noEncontrada(FacturaNoEncontradaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(NumeroFacturaDuplicadoException.class)
    public ProblemDetail duplicada(NumeroFacturaDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }

    @ExceptionHandler(FacturaInvalidaException.class)
    public ProblemDetail invalida(FacturaInvalidaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
