package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;

public record EstadisticaPeriodo(
        String periodo,
        int numeroEmitidas,
        BigDecimal totalEmitidas,
        int numeroRecibidas,
        BigDecimal totalRecibidas) {
}
