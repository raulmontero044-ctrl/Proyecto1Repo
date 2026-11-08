package com.facturacion.factura.api.facade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FacturaDto(
        Long id,
        String numero,
        String tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        Long empresaId,
        String empresaRazonSocial,
        String empresaNif,
        String empresaDireccion,
        Long terceroId,
        String terceroNombre,
        String terceroNif,
        String terceroDireccion,
        BigDecimal porcentajeIva,
        List<LineaFacturaDto> lineas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {
}
