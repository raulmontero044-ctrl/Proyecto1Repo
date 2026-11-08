package com.facturacion.pagos.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EstadoFacturaDto(
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
