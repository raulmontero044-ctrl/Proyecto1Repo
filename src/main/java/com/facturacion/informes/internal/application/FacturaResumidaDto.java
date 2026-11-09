package com.facturacion.informes.internal.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaResumidaDto(
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
