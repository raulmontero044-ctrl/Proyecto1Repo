package com.facturacion.usuarios.api.http.handlers;

import com.facturacion.usuarios.api.http.controllers.UsuarioController;
import com.facturacion.usuarios.internal.domain.exception.EmailUsuarioDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = UsuarioController.class)
public class EmailUsuarioDuplicadoExceptionHandler {

    @ExceptionHandler(EmailUsuarioDuplicadoException.class)
    public ProblemDetail duplicado(EmailUsuarioDuplicadoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
    }
}
