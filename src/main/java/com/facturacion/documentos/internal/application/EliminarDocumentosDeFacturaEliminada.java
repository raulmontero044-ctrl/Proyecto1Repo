package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EliminarDocumentosDeFacturaEliminada {

    private final DocumentoRepository repository;

    public EliminarDocumentosDeFacturaEliminada(DocumentoRepository repository) {
        this.repository = repository;
    }

    @EventListener
    @Transactional
    public void alEliminarFactura(FacturaEliminadaEvento evento) {
        repository.eliminarPorEntidad(EntidadTipo.FACTURA, evento.facturaId());
    }
}
