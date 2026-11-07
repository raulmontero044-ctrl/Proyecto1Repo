package com.facturacion.usuarios.api.http.handlers;

import com.facturacion.usuarios.api.http.controllers.UsuarioController;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UsuarioController.class)
public class UsuarioInvalidoExceptionHandler {

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ProblemDetail invalido(UsuarioInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }
}
