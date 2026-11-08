package com.facturacion.documentos.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento_contenido")
public class DocumentoContenidoEntity {

    @Id
    @Column(name = "documento_id", nullable = false)
    private Long documentoId;

    @Lob
    @Column(name = "contenido", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    protected DocumentoContenidoEntity() {
    }

    public DocumentoContenidoEntity(Long documentoId, byte[] contenido) {
        this.documentoId = documentoId;
        this.contenido = contenido;
    }

    public Long getDocumentoId() {
        return documentoId;
    }

    public byte[] getContenido() {
        return contenido;
    }
}
