package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;

public record ResumenTesoreria(BigDecimal cobros, BigDecimal pagos, BigDecimal saldoNeto) {
}
