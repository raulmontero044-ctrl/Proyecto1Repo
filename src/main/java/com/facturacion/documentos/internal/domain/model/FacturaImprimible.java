package com.facturacion.documentos.internal.domain.model;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FacturaImprimible(
        String numero,
        TipoFactura tipo,
        LocalDate fecha,
        LocalDate fechaVencimiento,
        Interviniente empresa,
        Interviniente tercero,
        BigDecimal porcentajeIva,
        List<LineaImprimible> lineas,
        BigDecimal base,
        BigDecimal cuotaIva,
        BigDecimal total) {

    public FacturaImprimible {
        if (tipo == null) {
            throw new DocumentoInvalidoException("El tipo de la factura es obligatorio");
        }
        if (empresa == null || tercero == null) {
            throw new DocumentoInvalidoException("La factura debe tener empresa y tercero");
        }
        lineas = lineas == null ? List.of() : List.copyOf(lineas);
    }

    public Interviniente emisor() {
        return tipo == TipoFactura.EMITIDA ? empresa : tercero;
    }

    public Interviniente receptor() {
        return tipo == TipoFactura.EMITIDA ? tercero : empresa;
    }
}
