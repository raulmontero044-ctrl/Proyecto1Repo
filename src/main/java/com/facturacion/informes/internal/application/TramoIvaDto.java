package com.facturacion.informes.internal.application;

import java.math.BigDecimal;

public record TramoIvaDto(BigDecimal porcentajeIva, BigDecimal base, BigDecimal cuota) {
}
