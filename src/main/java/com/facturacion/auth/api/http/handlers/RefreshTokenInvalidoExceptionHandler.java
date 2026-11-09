package com.facturacion.auth.api.http.handlers;

import com.facturacion.auth.api.http.controllers.AuthController;
import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class RefreshTokenInvalidoExceptionHandler {

    @ExceptionHandler(RefreshTokenInvalidoException.class)
    public ProblemDetail refreshTokenInvalido(RefreshTokenInvalidoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, excepcion.getMessage());
    }
}
