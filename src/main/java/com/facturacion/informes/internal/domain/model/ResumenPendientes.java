package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ResumenPendientes(
        BigDecimal pendienteCobro,
        BigDecimal vencidoCobro,
        BigDecimal pendientePago,
        BigDecimal vencidoPago,
        List<SaldoFactura> facturas) {

    public ResumenPendientes {
        facturas = List.copyOf(facturas);
    }
}
