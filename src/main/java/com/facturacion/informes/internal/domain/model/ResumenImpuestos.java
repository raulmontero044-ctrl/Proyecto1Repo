package com.facturacion.informes.internal.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record ResumenImpuestos(
        List<TramoIva> repercutido,
        BigDecimal cuotaRepercutida,
        List<TramoIva> soportado,
        BigDecimal cuotaSoportada,
        BigDecimal resultado) {

    public ResumenImpuestos {
        repercutido = List.copyOf(repercutido);
        soportado = List.copyOf(soportado);
    }
}
