package com.facturacion.informes.api.http.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenImpuestosResponse(
        List<TramoIvaResponse> repercutido,
        BigDecimal cuotaRepercutida,
        List<TramoIvaResponse> soportado,
        BigDecimal cuotaSoportada,
        BigDecimal resultado) {
}
