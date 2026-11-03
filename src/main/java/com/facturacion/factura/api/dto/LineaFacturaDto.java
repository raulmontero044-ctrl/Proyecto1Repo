package com.facturacion.factura.api.dto;

import java.math.BigDecimal;

public record LineaFacturaDto(String concepto, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
