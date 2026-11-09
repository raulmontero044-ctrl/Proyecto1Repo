package com.facturacion.informes.internal.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoFacturaDto(
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
