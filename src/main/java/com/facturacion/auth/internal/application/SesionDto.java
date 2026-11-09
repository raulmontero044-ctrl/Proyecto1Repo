package com.facturacion.auth.internal.application;

import java.time.Instant;

public record SesionDto(String accessToken, String refreshToken, Instant expiraEn) {
}
