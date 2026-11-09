package com.facturacion.auth.api.http.dto;

import jakarta.validation.constraints.NotBlank;

public record RenovarSesionRequest(@NotBlank String refreshToken) {
}
