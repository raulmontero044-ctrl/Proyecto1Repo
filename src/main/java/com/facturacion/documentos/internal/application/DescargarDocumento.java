package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DescargarDocumento {

    private final DocumentoRepository repository;

    public DescargarDocumento(DocumentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ContenidoDocumentoDto ejecutar(Long id) {
        Documento documento = repository.buscarPorId(id)
                .orElseThrow(() -> new DocumentoNoEncontradoException(id));
        byte[] contenido = repository.buscarContenido(id)
                .orElseThrow(() -> new DocumentoNoEncontradoException(id));
        return new ContenidoDocumentoDto(documento.nombre(), documento.tipoContenido().mime(), contenido);
    }
}
