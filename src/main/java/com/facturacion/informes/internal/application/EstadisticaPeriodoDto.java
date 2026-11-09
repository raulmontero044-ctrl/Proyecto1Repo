package com.facturacion.informes.internal.application;

import java.math.BigDecimal;

public record EstadisticaPeriodoDto(
        String periodo,
        int numeroEmitidas,
        BigDecimal totalEmitidas,
        int numeroRecibidas,
        BigDecimal totalRecibidas) {
}
