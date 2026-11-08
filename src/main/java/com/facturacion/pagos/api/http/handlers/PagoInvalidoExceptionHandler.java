package com.facturacion.pagos.api.http.handlers;

import com.facturacion.pagos.api.http.controllers.PagoController;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PagoController.class)
public class PagoInvalidoExceptionHandler {

    @ExceptionHandler(PagoInvalidoException.class)
    public ProblemDetail invalido(PagoInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
