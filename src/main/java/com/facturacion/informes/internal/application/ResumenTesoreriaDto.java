package com.facturacion.informes.internal.application;

import java.math.BigDecimal;

public record ResumenTesoreriaDto(BigDecimal cobros, BigDecimal pagos, BigDecimal saldoNeto) {
}
