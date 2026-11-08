package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.pagos.api.facade.PagoFacade;
import com.facturacion.terceros.api.facade.TerceroFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AdjuntarDocumento {

    private final DocumentoRepository repository;
    private final FacturaFacade facturaFacade;
    private final EmpresaFacade empresaFacade;
    private final TerceroFacade terceroFacade;
    private final PagoFacade pagoFacade;
    private final Clock reloj;

    public AdjuntarDocumento(
            DocumentoRepository repository,
            FacturaFacade facturaFacade,
            EmpresaFacade empresaFacade,
            TerceroFacade terceroFacade,
            PagoFacade pagoFacade,
            Clock reloj) {
        this.repository = repository;
        this.facturaFacade = facturaFacade;
        this.empresaFacade = empresaFacade;
        this.terceroFacade = terceroFacade;
        this.pagoFacade = pagoFacade;
        this.reloj = reloj;
    }

    @Transactional
    public DocumentoDto ejecutar(NuevoDocumentoDto nuevoDocumento) {
        byte[] contenido = nuevoDocumento.contenido() == null ? new byte[0] : nuevoDocumento.contenido();
        Documento documento = Documento.nuevo(
                EntidadTipo.desde(nuevoDocumento.entidadTipo()),
                nuevoDocumento.entidadId(),
                Documento.nombreDeArchivoAdjunto(nuevoDocumento.nombreArchivo()),
                TipoContenido.desdeMime(nuevoDocumento.contentType()),
                contenido.length,
                OrigenDocumento.ADJUNTO,
                Instant.now(reloj));
        if (!existe(documento.entidadTipo(), documento.entidadId())) {
            throw new DocumentoInvalidoException(
                    "No existe la entidad " + documento.entidadTipo() + " con id " + documento.entidadId());
        }
        return DocumentoDtoConverter.aDto(repository.guardar(documento, contenido));
    }

    private boolean existe(EntidadTipo entidadTipo, Long entidadId) {
        return switch (entidadTipo) {
            case FACTURA -> facturaFacade.buscar(entidadId).isPresent();
            case TERCERO -> terceroFacade.buscar(entidadId).isPresent();
            case EMPRESA -> empresaFacade.buscar(entidadId).isPresent();
            case PAGO -> pagoFacade.buscar(entidadId).isPresent();
        };
    }
}
