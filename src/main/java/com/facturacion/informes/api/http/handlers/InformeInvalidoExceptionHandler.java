package com.facturacion.informes.api.http.handlers;

import com.facturacion.informes.api.http.controllers.InformeController;
import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = InformeController.class)
public class InformeInvalidoExceptionHandler {

    @ExceptionHandler(InformeInvalidoException.class)
    public ProblemDetail invalido(InformeInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
