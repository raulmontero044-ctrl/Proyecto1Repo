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
        Long empresaId,
        String empresaRazonSocial,
        String empresaNif,
        String empresaDireccion,
        Long terceroId,
        String terceroNombre,
        String terceroNif,
        String terceroDireccion,
        BigDecimal porcentajeIva,
        List<LineaFactura> lineas) {

    private static final int LONGITUD_MAXIMA_DIRECCION = 600;

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
        if (terceroNombre == null || terceroNombre.isBlank()) {
            throw new FacturaInvalidaException("El nombre del tercero es obligatorio");
        }
        if (terceroNif == null || terceroNif.isBlank()) {
            throw new FacturaInvalidaException("El NIF del tercero es obligatorio");
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
            Long empresaId,
            String empresaRazonSocial,
            String empresaNif,
            String empresaDireccion,
            Long terceroId,
            String terceroNombre,
            String terceroNif,
            String terceroDireccion,
            BigDecimal porcentajeIva,
            List<LineaFactura> lineas) {
        if (empresaId == null) {
            throw new FacturaInvalidaException("La empresa de la factura es obligatoria");
        }
        exigirTexto(empresaRazonSocial, "La razón social de la empresa es obligatoria");
        exigirTexto(empresaNif, "El NIF de la empresa es obligatorio");
        exigirTexto(empresaDireccion, "La dirección de la empresa es obligatoria");
        limitarDireccion(empresaDireccion, "La dirección de la empresa no puede superar 600 caracteres");
        if (terceroId == null) {
            throw new FacturaInvalidaException("El tercero de la factura es obligatorio");
        }
        limitarDireccion(terceroDireccion, "La dirección del tercero no puede superar 600 caracteres");
        return new Factura(
                null,
                numero,
                tipo,
                fecha,
                fechaVencimiento,
                empresaId,
                empresaRazonSocial,
                empresaNif,
                empresaDireccion,
                terceroId,
                terceroNombre,
                terceroNif,
                terceroDireccion,
                porcentajeIva,
                lineas);
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new FacturaInvalidaException(mensaje);
        }
    }

    private static void limitarDireccion(String direccion, String mensaje) {
        if (direccion != null && direccion.length() > LONGITUD_MAXIMA_DIRECCION) {
            throw new FacturaInvalidaException(mensaje);
        }
    }
}
