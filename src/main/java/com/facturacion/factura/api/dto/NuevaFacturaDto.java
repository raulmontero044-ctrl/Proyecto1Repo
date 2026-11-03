package com.facturacion.factura.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record NuevaFacturaDto(
        String numero,
        LocalDate fecha,
        String clienteNombre,
        String clienteNif,
        BigDecimal porcentajeIva,
        List<NuevaLineaFacturaDto> lineas) {
}
