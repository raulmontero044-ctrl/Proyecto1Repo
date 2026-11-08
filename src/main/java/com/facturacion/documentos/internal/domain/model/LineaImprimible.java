package com.facturacion.documentos.internal.domain.model;

import java.math.BigDecimal;

public record LineaImprimible(String concepto, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importe) {
}
