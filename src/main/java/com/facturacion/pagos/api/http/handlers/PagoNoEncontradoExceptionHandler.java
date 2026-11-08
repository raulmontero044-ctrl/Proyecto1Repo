package com.facturacion.pagos.api.http.handlers;

import com.facturacion.pagos.api.http.controllers.PagoController;
import com.facturacion.pagos.internal.domain.exception.PagoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PagoController.class)
public class PagoNoEncontradoExceptionHandler {

    @ExceptionHandler(PagoNoEncontradoException.class)
    public ProblemDetail noEncontrado(PagoNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
