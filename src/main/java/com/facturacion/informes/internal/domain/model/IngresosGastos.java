package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;

public record IngresosGastos(BigDecimal ingresos, BigDecimal gastos, BigDecimal resultado) {
}
