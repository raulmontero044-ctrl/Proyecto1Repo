package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenFacturasResponse(
        List<FacturaResumidaResponse> facturas,
        int numeroFacturas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
