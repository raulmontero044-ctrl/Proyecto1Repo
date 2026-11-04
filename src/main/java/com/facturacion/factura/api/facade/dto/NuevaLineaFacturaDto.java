package com.facturacion.factura.api.facade.dto;

import java.math.BigDecimal;

public record NuevaLineaFacturaDto(String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {
}
