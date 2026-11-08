package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.FacturaImprimible;
import com.facturacion.documentos.internal.domain.model.Interviniente;
import com.facturacion.documentos.internal.domain.model.LineaImprimible;
import com.facturacion.documentos.internal.domain.model.TipoFactura;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;

import java.util.List;

final class DocumentoDtoConverter {

    private DocumentoDtoConverter() {
    }

    static DocumentoDto aDto(Documento documento) {
        return new DocumentoDto(
                documento.id(),
                documento.entidadTipo().name(),
                documento.entidadId(),
                documento.nombre(),
                documento.tipoContenido().mime(),
                documento.tamano(),
                documento.origen().name(),
                documento.creadoEn());
    }

    static FacturaImprimible aFacturaImprimible(FacturaDto factura) {
        return new FacturaImprimible(
                factura.numero(),
                TipoFactura.desde(factura.tipo()),
                factura.fecha(),
                factura.fechaVencimiento(),
                new Interviniente(factura.empresaRazonSocial(), factura.empresaNif(), factura.empresaDireccion()),
                new Interviniente(factura.terceroNombre(), factura.terceroNif(), factura.terceroDireccion()),
                factura.porcentajeIva(),
                aLineas(factura.lineas()),
                factura.base(),
                factura.cuotaIva(),
                factura.total());
    }

    private static List<LineaImprimible> aLineas(List<LineaFacturaDto> lineas) {
        if (lineas == null) {
            return List.of();
        }
        return lineas.stream()
                .map(linea -> new LineaImprimible(
                        linea.concepto(), linea.cantidad(), linea.precioUnitario(), linea.importe()))
                .toList();
    }
}
