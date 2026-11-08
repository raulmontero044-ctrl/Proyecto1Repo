package com.facturacion.pagos.api.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponse(
        Long id,
        Long facturaId,
        Long empresaId,
        String tipo,
        LocalDate fecha,
        BigDecimal importe,
        String metodo,
        String referencia) {
}
