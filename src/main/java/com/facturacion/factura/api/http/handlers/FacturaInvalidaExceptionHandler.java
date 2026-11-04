package com.facturacion.factura.api.http.handlers;

import com.facturacion.factura.api.http.controllers.FacturaController;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = FacturaController.class)
public class FacturaInvalidaExceptionHandler {

    @ExceptionHandler(FacturaInvalidaException.class)
    public ProblemDetail invalida(FacturaInvalidaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
