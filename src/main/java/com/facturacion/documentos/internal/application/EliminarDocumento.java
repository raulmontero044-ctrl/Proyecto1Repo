package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoNoEncontradoException;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarDocumento {

    private final DocumentoRepository repository;

    public EliminarDocumento(DocumentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id) {
        if (repository.buscarPorId(id).isEmpty()) {
            throw new DocumentoNoEncontradoException(id);
        }
        repository.eliminar(id);
    }
}
