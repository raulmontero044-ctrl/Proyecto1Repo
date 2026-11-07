package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.LineaFactura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.domain.service.CalculadoraFactura;

import java.util.List;

final class FacturaDtoConverter {

    private FacturaDtoConverter() {
    }

    static Factura aDominio(NuevaFacturaDto dto) {
        if (dto.lineas() == null) {
            throw new FacturaInvalidaException("La factura debe tener al menos una línea");
        }
        List<LineaFactura> lineas = dto.lineas().stream()
                .map(linea -> {
                    if (linea == null) {
                        throw new FacturaInvalidaException("La factura no puede contener líneas vacías");
                    }
                    return new LineaFactura(linea.concepto(), linea.cantidad(), linea.precioUnitario());
                })
                .toList();
        return Factura.nueva(
                dto.numero(),
                aTipo(dto.tipo()),
                dto.fecha(),
                dto.fechaVencimiento(),
                dto.clienteNombre(),
                dto.clienteNif(),
                dto.porcentajeIva(),
                lineas);
    }

    private static TipoFactura aTipo(String tipo) {
        if (tipo == null) {
            throw new FacturaInvalidaException("El tipo de factura es obligatorio");
        }
        try {
            return TipoFactura.valueOf(tipo);
        } catch (IllegalArgumentException excepcion) {
            throw new FacturaInvalidaException("El tipo de factura debe ser EMITIDA o RECIBIDA");
        }
    }

    static FacturaDto aDto(Factura factura) {
        List<LineaFacturaDto> lineas = factura.lineas().stream()
                .map(linea -> new LineaFacturaDto(
                        linea.concepto(),
                        linea.cantidad(),
                        linea.precioUnitario(),
                        CalculadoraFactura.importe(linea)))
                .toList();
        return new FacturaDto(
                factura.id(),
                factura.numero(),
                factura.tipo().name(),
                factura.fecha(),
                factura.fechaVencimiento(),
                factura.clienteNombre(),
                factura.clienteNif(),
                factura.porcentajeIva(),
                lineas,
                CalculadoraFactura.base(factura),
                CalculadoraFactura.cuotaIva(factura),
                CalculadoraFactura.total(factura));
    }
}
