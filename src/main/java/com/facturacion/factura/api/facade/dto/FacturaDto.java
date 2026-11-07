package com.facturacion.factura.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FacturaDto(
        Long id,
        String numero,
        String tipo,
        LocalDate fecha,
        String clienteNombre,
        String clienteNif,
        BigDecimal porcentajeIva,
        List<LineaFacturaDto> lineas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
