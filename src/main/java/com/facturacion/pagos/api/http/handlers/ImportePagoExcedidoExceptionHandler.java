package com.facturacion.pagos.api.http.handlers;

import com.facturacion.pagos.api.http.controllers.PagoController;
import com.facturacion.pagos.internal.domain.exception.ImportePagoExcedidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PagoController.class)
public class ImportePagoExcedidoExceptionHandler {

    @ExceptionHandler(ImportePagoExcedidoException.class)
    public ProblemDetail excedido(ImportePagoExcedidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
