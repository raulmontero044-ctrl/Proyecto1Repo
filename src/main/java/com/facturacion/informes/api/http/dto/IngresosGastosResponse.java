package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;

public record IngresosGastosResponse(BigDecimal ingresos, BigDecimal gastos, BigDecimal resultado) {
}
