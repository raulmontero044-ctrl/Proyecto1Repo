package com.facturacion.factura.api.http.handlers;

import com.facturacion.factura.api.http.controllers.FacturaController;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FacturaController.class)
public class NumeroFacturaDuplicadoExceptionHandler {

    @ExceptionHandler(NumeroFacturaDuplicadoException.class)
    public ProblemDetail duplicada(NumeroFacturaDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
