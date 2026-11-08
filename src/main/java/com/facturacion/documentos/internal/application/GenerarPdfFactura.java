package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.FacturaInexistenteException;
import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.documentos.internal.domain.service.GeneradorPdfFactura;
import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class GenerarPdfFactura {

    private final DocumentoRepository repository;
    private final FacturaFacade facturaFacade;
    private final GeneradorPdfFactura generador;
    private final Clock reloj;

    public GenerarPdfFactura(
            DocumentoRepository repository,
            FacturaFacade facturaFacade,
            GeneradorPdfFactura generador,
            Clock reloj) {
        this.repository = repository;
        this.facturaFacade = facturaFacade;
        this.generador = generador;
        this.reloj = reloj;
    }

    @Transactional
    public DocumentoDto ejecutar(Long facturaId) {
        FacturaDto factura = facturaFacade.buscar(facturaId)
                .orElseThrow(() -> new FacturaInexistenteException(facturaId));
        byte[] pdf = generador.generar(DocumentoDtoConverter.aFacturaImprimible(factura));
        Documento documento = Documento.nuevo(
                EntidadTipo.FACTURA,
                factura.id(),
                Documento.nombreDePdfDeFactura(factura.numero()),
                TipoContenido.PDF,
                pdf.length,
                OrigenDocumento.GENERADO,
                Instant.now(reloj));
        return DocumentoDtoConverter.aDto(repository.guardar(documento, pdf));
    }
}
