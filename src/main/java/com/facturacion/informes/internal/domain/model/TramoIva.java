package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;

public record TramoIva(BigDecimal porcentajeIva, BigDecimal base, BigDecimal cuota) {
}
