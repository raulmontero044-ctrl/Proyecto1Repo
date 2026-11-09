package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;

public record ResumenTesoreriaResponse(BigDecimal cobros, BigDecimal pagos, BigDecimal saldoNeto) {
}
