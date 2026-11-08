package com.facturacion.pagos.api.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoFacturaResponse(
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
