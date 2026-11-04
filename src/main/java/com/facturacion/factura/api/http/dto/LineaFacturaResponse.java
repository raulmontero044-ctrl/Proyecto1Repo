package com.facturacion.factura.api.http.dto;

import java.math.BigDecimal;

public record LineaFacturaResponse(String concepto, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
