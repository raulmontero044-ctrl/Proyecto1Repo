package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.exception.DocumentoInvalidoException;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarDocumentos {

    private final DocumentoRepository repository;

    public ListarDocumentos(DocumentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDto> ejecutar(String entidadTipo, Long entidadId) {
        EntidadTipo tipo = EntidadTipo.desde(entidadTipo);
        if (entidadId == null) {
            throw new DocumentoInvalidoException("La entidad del documento es obligatoria");
        }
        return repository.listarPorEntidad(tipo, entidadId).stream()
                .map(DocumentoDtoConverter::aDto)
                .toList();
    }
}
