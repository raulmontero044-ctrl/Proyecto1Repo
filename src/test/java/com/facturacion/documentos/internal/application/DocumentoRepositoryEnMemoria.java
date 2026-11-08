package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class DocumentoRepositoryEnMemoria implements DocumentoRepository {

    private final Map<Long, Documento> documentos = new LinkedHashMap<>();
    private final Map<Long, byte[]> contenidos = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Documento guardar(Documento documento, byte[] contenido) {
        Long id = documento.id() != null ? documento.id() : siguienteId++;
        Documento guardado = new Documento(
                id,
                documento.entidadTipo(),
                documento.entidadId(),
                documento.nombre(),
                documento.tipoContenido(),
                documento.tamano(),
                documento.origen(),
                documento.creadoEn());
        documentos.put(id, guardado);
        contenidos.put(id, contenido.clone());
        return guardado;
    }

    @Override
    public Optional<Documento> buscarPorId(Long id) {
        return Optional.ofNullable(documentos.get(id));
    }

    @Override
    public Optional<byte[]> buscarContenido(Long id) {
        return Optional.ofNullable(contenidos.get(id)).map(byte[]::clone);
    }

    @Override
    public List<Documento> listarPorEntidad(EntidadTipo entidadTipo, Long entidadId) {
        return documentos.values().stream()
                .filter(documento -> documento.entidadTipo() == entidadTipo
                        && documento.entidadId().equals(entidadId))
                .sorted(Comparator.comparing(Documento::id))
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        documentos.remove(id);
        contenidos.remove(id);
    }

    @Override
    public void eliminarPorEntidad(EntidadTipo entidadTipo, Long entidadId) {
        listarPorEntidad(entidadTipo, entidadId).forEach(documento -> eliminar(documento.id()));
    }

    List<Documento> todos() {
        return List.copyOf(documentos.values());
    }

    Map<Long, byte[]> contenidos() {
        return Map.copyOf(contenidos);
    }
}
