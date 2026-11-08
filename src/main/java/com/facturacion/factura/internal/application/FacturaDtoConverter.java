package com.facturacion.factura.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.LineaFactura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.domain.service.CalculadoraFactura;
import com.facturacion.terceros.api.facade.dto.TerceroDto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FacturaDtoConverter {

    private FacturaDtoConverter() {
    }

    static Factura aDominio(NuevaFacturaDto dto, EmpresaDto empresa, TerceroDto tercero) {
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
                empresa.id(),
                empresa.razonSocial(),
                empresa.nif(),
                direccionCompleta(
                        empresa.direccion(), empresa.codigoPostal(), empresa.municipio(), empresa.provincia(), empresa.pais()),
                tercero.id(),
                tercero.razonSocial(),
                tercero.nif(),
                direccionCompleta(
                        tercero.direccion(), tercero.codigoPostal(), tercero.municipio(), tercero.provincia(), tercero.pais()),
                dto.porcentajeIva(),
                lineas);
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
                factura.empresaId(),
                factura.empresaRazonSocial(),
                factura.empresaNif(),
                factura.empresaDireccion(),
                factura.terceroId(),
                factura.terceroNombre(),
                factura.terceroNif(),
                factura.terceroDireccion(),
                factura.porcentajeIva(),
                lineas,
                CalculadoraFactura.base(factura),
                CalculadoraFactura.cuotaIva(factura),
                CalculadoraFactura.total(factura));
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

    private static String direccionCompleta(String... partes) {
        String direccion = Stream.of(partes)
                .filter(parte -> parte != null && !parte.isBlank())
                .map(String::strip)
                .collect(Collectors.joining(", "));
        return direccion.isEmpty() ? null : direccion;
    }
}
