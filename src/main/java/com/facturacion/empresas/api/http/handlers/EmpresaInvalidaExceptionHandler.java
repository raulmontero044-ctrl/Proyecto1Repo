package com.facturacion.empresas.api.http.handlers;

import com.facturacion.empresas.api.http.controllers.EmpresaController;
import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmpresaController.class)
public class EmpresaInvalidaExceptionHandler {

    @ExceptionHandler(EmpresaInvalidaException.class)
    public ProblemDetail invalida(EmpresaInvalidaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
