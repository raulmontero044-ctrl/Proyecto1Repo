package com.facturacion.factura.api.dto;

import java.math.BigDecimal;

public record NuevaLineaFacturaDto(String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {
}
