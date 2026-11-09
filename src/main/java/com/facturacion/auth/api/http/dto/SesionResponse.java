package com.facturacion.auth.api.http.dto;

import java.time.Instant;

public record SesionResponse(String accessToken, String refreshToken, Instant expiraEn) {
}
