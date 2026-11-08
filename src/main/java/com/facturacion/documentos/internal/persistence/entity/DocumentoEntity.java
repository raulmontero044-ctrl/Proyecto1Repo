package com.facturacion.documentos.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "documento",
        indexes = @Index(name = "ix_documento_entidad", columnList = "entidad_tipo, entidad_id"))
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidad_tipo", nullable = false, length = 10)
    private String entidadTipo;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "tamano", nullable = false)
    private long tamano;

    @Column(name = "origen", nullable = false, length = 10)
    private String origen;

    @Column(name = "creado_en", nullable = false, columnDefinition = "DATETIME(6)")
    private Instant creadoEn;

    protected DocumentoEntity() {
    }

    public DocumentoEntity(
            Long id,
            String entidadTipo,
            Long entidadId,
            String nombre,
            String contentType,
            long tamano,
            String origen,
            Instant creadoEn) {
        this.id = id;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.nombre = nombre;
        this.contentType = contentType;
        this.tamano = tamano;
        this.origen = origen;
        this.creadoEn = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getEntidadTipo() {
        return entidadTipo;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTamano() {
        return tamano;
    }

    public String getOrigen() {
        return origen;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
