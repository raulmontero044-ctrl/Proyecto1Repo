package com.facturacion.factura.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NuevaFacturaDto(
        String numero,
        String tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        Long empresaId,
        Long terceroId,
        BigDecimal porcentajeIva,
        List<NuevaLineaFacturaDto> lineas) {
}
