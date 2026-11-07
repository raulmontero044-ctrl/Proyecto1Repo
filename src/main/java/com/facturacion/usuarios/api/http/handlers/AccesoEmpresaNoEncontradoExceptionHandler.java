package com.facturacion.usuarios.api.http.handlers;

import com.facturacion.usuarios.api.http.controllers.UsuarioController;
import com.facturacion.usuarios.internal.domain.exception.AccesoEmpresaNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UsuarioController.class)
public class AccesoEmpresaNoEncontradoExceptionHandler {

    @ExceptionHandler(AccesoEmpresaNoEncontradoException.class)
    public ProblemDetail noEncontrado(AccesoEmpresaNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }
}
