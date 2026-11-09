package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenPendientesResponse(
        BigDecimal pendienteCobro,
        BigDecimal vencidoCobro,
        BigDecimal pendientePago,
        BigDecimal vencidoPago,
        List<SaldoFacturaResponse> facturas) {
}
