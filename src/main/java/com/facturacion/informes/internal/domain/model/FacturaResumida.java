package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaResumida(
        Long id,
        String numero,
        TipoFactura tipo,
        LocalDate fecha,
        Long terceroId,
        String terceroNombre,
        BigDecimal porcentajeIva,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {

    public FacturaResumida {
        if (tipo == null) {
            throw new InformeInvalidoException("El tipo de la factura es obligatorio");
        }
        if (fecha == null) {
            throw new InformeInvalidoException("La fecha de la factura es obligatoria");
        }
        if (porcentajeIva == null) {
            throw new InformeInvalidoException("El porcentaje de IVA de la factura es obligatorio");
        }
        if (base == null || cuotaIva == null || total == null) {
            throw new InformeInvalidoException("La base, la cuota de IVA y el total de la factura son obligatorios");
        }
    }
}
