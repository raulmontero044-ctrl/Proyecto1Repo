package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;

public record EstadisticaPeriodoResponse(
        String periodo,
        int numeroEmitidas,
        BigDecimal totalEmitidas,
        int numeroRecibidas,
        BigDecimal totalRecibidas) {
}
