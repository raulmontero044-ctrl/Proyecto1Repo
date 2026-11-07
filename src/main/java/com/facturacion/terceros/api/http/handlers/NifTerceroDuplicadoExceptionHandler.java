package com.facturacion.terceros.api.http.handlers;

import com.facturacion.terceros.api.http.controllers.TerceroController;
import com.facturacion.terceros.internal.domain.exception.NifTerceroDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TerceroController.class)
public class NifTerceroDuplicadoExceptionHandler {

    @ExceptionHandler(NifTerceroDuplicadoException.class)
    public ProblemDetail duplicado(NifTerceroDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
