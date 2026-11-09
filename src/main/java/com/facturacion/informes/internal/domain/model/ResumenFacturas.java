package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ResumenFacturas(
        List<FacturaResumida> facturas,
        int numeroFacturas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {

    public ResumenFacturas {
        facturas = List.copyOf(facturas);
    }
}
