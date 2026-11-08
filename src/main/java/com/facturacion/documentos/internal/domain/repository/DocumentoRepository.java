package com.facturacion.documentos.internal.domain.repository;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository {

    Documento guardar(Documento documento, byte[] contenido);

    Optional<Documento> buscarPorId(Long id);

    Optional<byte[]> buscarContenido(Long id);

    List<Documento> listarPorEntidad(EntidadTipo entidadTipo, Long entidadId);

    void eliminar(Long id);

    void eliminarPorEntidad(EntidadTipo entidadTipo, Long entidadId);
}
