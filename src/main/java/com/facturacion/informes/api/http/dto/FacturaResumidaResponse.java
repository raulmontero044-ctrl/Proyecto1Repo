package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaResumidaResponse(
        Long id,
        String numero,
        String tipo,
        LocalDate fecha,
        Long terceroId,
        String terceroNombre,
        BigDecimal porcentajeIva,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
