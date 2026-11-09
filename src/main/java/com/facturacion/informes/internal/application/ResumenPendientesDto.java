package com.facturacion.informes.internal.application;

import java.math.BigDecimal;
import java.util.List;

public record ResumenPendientesDto(
        BigDecimal pendienteCobro,
        BigDecimal vencidoCobro,
        BigDecimal pendientePago,
        BigDecimal vencidoPago,
        List<SaldoFacturaDto> facturas) {
}
