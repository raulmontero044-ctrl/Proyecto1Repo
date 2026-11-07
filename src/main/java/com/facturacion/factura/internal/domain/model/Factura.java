package com.facturacion.factura.internal.domain.model;

import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record Factura(
        Long id,
        String numero,
        TipoFactura tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        String clienteNombre,
        String clienteNif,
        BigDecimal porcentajeIva,
        List<LineaFactura> lineas) {

    public Factura {
        if (numero == null || numero.isBlank()) {
            throw new FacturaInvalidaException("El número de factura es obligatorio");
        }
        if (tipo == null) {
            throw new FacturaInvalidaException("El tipo de factura es obligatorio");
        }
        if (fecha == null) {
            throw new FacturaInvalidaException("La fecha de la factura es obligatoria");
        }
        if (fechaVencimiento != null && fechaVencimiento.isBefore(fecha)) {
            throw new FacturaInvalidaException("La fecha de vencimiento no puede ser anterior a la fecha de la factura");
        }
        if (clienteNombre == null || clienteNombre.isBlank()) {
            throw new FacturaInvalidaException("El nombre del cliente es obligatorio");
        }
        if (clienteNif == null || clienteNif.isBlank()) {
            throw new FacturaInvalidaException("El NIF del cliente es obligatorio");
        }
        if (porcentajeIva == null || porcentajeIva.signum() < 0) {
            throw new FacturaInvalidaException("El porcentaje de IVA no puede ser negativo");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new FacturaInvalidaException("La factura debe tener al menos una línea");
        }
        if (lineas.stream().anyMatch(Objects::isNull)) {
            throw new FacturaInvalidaException("La factura no puede contener líneas vacías");
        }
        lineas = List.copyOf(lineas);
    }

    public static Factura nueva(
            String numero,
            TipoFactura tipo,
            LocalDate fecha,
            LocalDate fechaVencimiento,
            String clienteNombre,
            String clienteNif,
            BigDecimal porcentajeIva,
            List<LineaFactura> lineas) {
        return new Factura(null, numero, tipo, fecha, fechaVencimiento, clienteNombre, clienteNif, porcentajeIva, lineas);
    }
}
