package com.facturacion.factura.api.http.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FacturaResponse(
        Long id,
        String numero,
        String tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        String clienteNombre,
        String clienteNif,
        BigDecimal porcentajeIva,
        List<LineaFacturaResponse> lineas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
