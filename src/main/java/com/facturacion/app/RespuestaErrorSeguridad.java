package com.facturacion.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;

class RespuestaErrorSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    static final String NO_AUTENTICADO = "Se requiere un token de acceso válido";
    static final String SIN_PERMISO = "No tiene permiso para realizar esta operación";

    private final ObjectMapper objectMapper;

    RespuestaErrorSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException excepcion)
            throws IOException {
        respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        escribir(peticion, respuesta, HttpStatus.UNAUTHORIZED, NO_AUTENTICADO);
    }

    @Override
    public void handle(
            HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException excepcion)
            throws IOException {
        escribir(peticion, respuesta, HttpStatus.FORBIDDEN, SIN_PERMISO);
    }

    private void escribir(HttpServletRequest peticion, HttpServletResponse respuesta, HttpStatus estado, String detalle)
            throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setInstance(URI.create(peticion.getRequestURI()));
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(respuesta.getOutputStream(), problema);
    }
}
