package com.facturacion.documentos.internal.persistence.dao;

import com.facturacion.documentos.internal.persistence.entity.DocumentoContenidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoContenidoDao extends JpaRepository<DocumentoContenidoEntity, Long> {
}
