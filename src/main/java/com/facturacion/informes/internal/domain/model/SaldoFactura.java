package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoFactura(
        Long facturaId,
        String numero,
        TipoFactura tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        BigDecimal total,
        BigDecimal pagado,
        BigDecimal pendiente,
        EstadoCobro estado,
        boolean vencida) {

    public SaldoFactura {
        if (tipo == null) {
            throw new InformeInvalidoException("El tipo de la factura es obligatorio");
        }
        if (estado == null) {
            throw new InformeInvalidoException("El estado de la factura es obligatorio");
        }
        if (fecha == null) {
            throw new InformeInvalidoException("La fecha de la factura es obligatoria");
        }
        if (total == null || pagado == null || pendiente == null) {
            throw new InformeInvalidoException("El total, lo pagado y lo pendiente de la factura son obligatorios");
        }
    }
}
