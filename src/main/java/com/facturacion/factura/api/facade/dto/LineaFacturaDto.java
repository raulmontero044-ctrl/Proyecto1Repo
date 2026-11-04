package com.facturacion.factura.api.facade.dto;

import java.math.BigDecimal;

public record LineaFacturaDto(String concepto, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
