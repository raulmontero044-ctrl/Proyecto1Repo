package com.facturacion.pagos.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NuevoPagoDto(
        Long facturaId,
        LocalDate fecha,
        BigDecimal importe,
        String metodo,
        String referencia) {
}
