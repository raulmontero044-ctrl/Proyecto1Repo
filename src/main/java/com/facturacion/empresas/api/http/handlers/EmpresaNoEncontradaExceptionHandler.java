package com.facturacion.empresas.api.http.handlers;

import com.facturacion.empresas.api.http.controllers.EmpresaController;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmpresaController.class)
public class EmpresaNoEncontradaExceptionHandler {

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    public ProblemDetail noEncontrada(EmpresaNoEncontradaException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
