package com.facturacion.informes.internal.application;

import java.math.BigDecimal;

public record IngresosGastosDto(BigDecimal ingresos, BigDecimal gastos, BigDecimal resultado) {
}
