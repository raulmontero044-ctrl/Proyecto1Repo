package com.facturacion.pagos.internal.domain.model;

import java.math.BigDecimal;

public record SituacionFactura(
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal pendiente,
        EstadoFactura estado,
        boolean vencida) {
}
