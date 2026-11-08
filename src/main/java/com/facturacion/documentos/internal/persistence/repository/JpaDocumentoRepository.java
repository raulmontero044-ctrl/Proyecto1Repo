package com.facturacion.documentos.internal.persistence.repository;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.documentos.internal.persistence.dao.DocumentoContenidoDao;
import com.facturacion.documentos.internal.persistence.dao.DocumentoDao;
import com.facturacion.documentos.internal.persistence.entity.DocumentoContenidoEntity;
import com.facturacion.documentos.internal.persistence.entity.DocumentoEntity;
import com.facturacion.documentos.internal.persistence.mapper.DocumentoMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaDocumentoRepository implements DocumentoRepository {

    private final DocumentoDao dao;
    private final DocumentoContenidoDao contenidoDao;
    private final DocumentoMapper mapper;

    public JpaDocumentoRepository(DocumentoDao dao, DocumentoContenidoDao contenidoDao, DocumentoMapper mapper) {
        this.dao = dao;
        this.contenidoDao = contenidoDao;
        this.mapper = mapper;
    }

    @Override
    public Documento guardar(Documento documento, byte[] contenido) {
        DocumentoEntity guardada = dao.save(mapper.aEntidad(documento));
        contenidoDao.save(new DocumentoContenidoEntity(guardada.getId(), contenido));
        return mapper.aDominio(guardada);
    }

    @Override
    public Optional<Documento> buscarPorId(Long id) {
        return dao.findById(id).map(mapper::aDominio);
    }

    @Override
    public Optional<byte[]> buscarContenido(Long id) {
        return contenidoDao.findById(id).map(DocumentoContenidoEntity::getContenido);
    }

    @Override
    public List<Documento> listarPorEntidad(EntidadTipo entidadTipo, Long entidadId) {
        return dao.findAllByEntidadTipoAndEntidadIdOrderByIdAsc(entidadTipo.name(), entidadId).stream()
                .map(mapper::aDominio)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        contenidoDao.deleteById(id);
        dao.deleteById(id);
    }

    @Override
    public void eliminarPorEntidad(EntidadTipo entidadTipo, Long entidadId) {
        List<DocumentoEntity> entidades =
                dao.findAllByEntidadTipoAndEntidadIdOrderByIdAsc(entidadTipo.name(), entidadId);
        contenidoDao.deleteAllById(entidades.stream().map(DocumentoEntity::getId).toList());
        dao.deleteAll(entidades);
    }
}
