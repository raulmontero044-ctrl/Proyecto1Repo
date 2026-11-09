package com.facturacion.auth.api.http.controllers;

import com.facturacion.auth.api.http.dto.CerrarSesionRequest;
import com.facturacion.auth.api.http.dto.IniciarSesionRequest;
import com.facturacion.auth.api.http.dto.RenovarSesionRequest;
import com.facturacion.auth.api.http.dto.SesionResponse;
import com.facturacion.auth.internal.application.CerrarSesion;
import com.facturacion.auth.internal.application.IniciarSesion;
import com.facturacion.auth.internal.application.RenovarSesion;
import com.facturacion.auth.internal.application.SesionDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IniciarSesion iniciarSesion;
    private final RenovarSesion renovarSesion;
    private final CerrarSesion cerrarSesion;

    public AuthController(IniciarSesion iniciarSesion, RenovarSesion renovarSesion, CerrarSesion cerrarSesion) {
        this.iniciarSesion = iniciarSesion;
        this.renovarSesion = renovarSesion;
        this.cerrarSesion = cerrarSesion;
    }

    @PostMapping("/login")
    public SesionResponse iniciar(@Valid @RequestBody IniciarSesionRequest request) {
        return aRespuesta(iniciarSesion.ejecutar(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public SesionResponse renovar(@Valid @RequestBody RenovarSesionRequest request) {
        return aRespuesta(renovarSesion.ejecutar(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrar(@Valid @RequestBody CerrarSesionRequest request) {
        cerrarSesion.ejecutar(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private static SesionResponse aRespuesta(SesionDto sesion) {
        return new SesionResponse(sesion.accessToken(), sesion.refreshToken(), sesion.expiraEn());
    }
}
