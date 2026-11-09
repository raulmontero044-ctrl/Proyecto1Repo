package com.facturacion.auth.api.http.dto;

import jakarta.validation.constraints.NotBlank;

public record CerrarSesionRequest(@NotBlank String refreshToken) {
}
