package com.facturacion.empresas.api.http.handlers;

import com.facturacion.empresas.api.http.controllers.EmpresaController;
import com.facturacion.empresas.internal.domain.exception.NifEmpresaDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmpresaController.class)
public class NifEmpresaDuplicadoExceptionHandler {

    @ExceptionHandler(NifEmpresaDuplicadoException.class)
    public ProblemDetail duplicado(NifEmpresaDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
