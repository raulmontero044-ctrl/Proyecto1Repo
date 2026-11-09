package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;

public record TramoIvaResponse(BigDecimal porcentajeIva, BigDecimal base, BigDecimal cuota) {
}
