package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoFacturaResponse(
        Long facturaId,
        String numero,
        String tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal pendiente,
        String estado,
        boolean vencida) {
}
