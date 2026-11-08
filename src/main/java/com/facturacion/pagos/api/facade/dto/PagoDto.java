package com.facturacion.pagos.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoDto(
        Long id,
        Long facturaId,
        Long empresaId,
        String tipo,
        LocalDate fecha,
        BigDecimal importe,
        String metodo,
        String referencia) {
}
