package com.facturacion.documentos.internal.persistence.dao;

import com.facturacion.documentos.internal.persistence.entity.DocumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoDao extends JpaRepository<DocumentoEntity, Long> {

    List<DocumentoEntity> findAllByEntidadTipoAndEntidadIdOrderByIdAsc(String entidadTipo, Long entidadId);
}
