package com.facturacion.factura.apln;

import com.facturacion.factura.api.dto.FacturaDto;
import com.facturacion.factura.api.dto.LineaFacturaDto;
import com.facturacion.factura.api.dto.NuevaFacturaDto;
import com.facturacion.factura.domain.model.Factura;
import com.facturacion.factura.domain.model.FacturaInvalidaException;
import com.facturacion.factura.domain.model.LineaFactura;

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
                dto.fecha(),
                dto.clienteNombre(),
                dto.clienteNif(),
                dto.porcentajeIva(),
                lineas);
    }

    static FacturaDto aDto(Factura factura) {
        List<LineaFacturaDto> lineas = factura.lineas().stream()
                .map(linea -> new LineaFacturaDto(
                        linea.concepto(),
                        linea.cantidad(),
                        linea.precioUnitario(),
                        linea.importe()))
                .toList();
        return new FacturaDto(
                factura.id(),
                factura.numero(),
                factura.fecha(),
                factura.clienteNombre(),
                factura.clienteNif(),
                factura.porcentajeIva(),
                lineas,
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }
}
