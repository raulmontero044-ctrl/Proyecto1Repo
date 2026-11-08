package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerDocumento {

    private final DocumentoRepository repository;

    public ObtenerDocumento(DocumentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DocumentoDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(DocumentoDtoConverter::aDto)
                .orElseThrow(() -> new DocumentoNoEncontradoException(id));
    }
}
