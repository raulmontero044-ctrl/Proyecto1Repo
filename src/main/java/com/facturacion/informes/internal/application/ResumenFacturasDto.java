package com.facturacion.informes.internal.application;

import java.math.BigDecimal;
import java.util.List;

public record ResumenFacturasDto(
        List<FacturaResumidaDto> facturas,
        int numeroFacturas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
