package com.facturacion.informes.internal.application;

import java.math.BigDecimal;
import java.util.List;

public record ResumenImpuestosDto(
        List<TramoIvaDto> repercutido,
        BigDecimal cuotaRepercutida,
        List<TramoIvaDto> soportado,
        BigDecimal cuotaSoportada,
        BigDecimal resultado) {
}
