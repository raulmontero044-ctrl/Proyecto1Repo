package com.facturacion.auth.api.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IniciarSesionRequest(
        @NotBlank @Size(max = 200) String email,
        @NotBlank String password) {
}
