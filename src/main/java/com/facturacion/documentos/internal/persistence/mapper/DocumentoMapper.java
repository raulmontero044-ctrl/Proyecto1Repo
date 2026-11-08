package com.facturacion.documentos.internal.persistence.mapper;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.persistence.entity.DocumentoEntity;
import org.springframework.stereotype.Component;

@Component
public class DocumentoMapper {

    public DocumentoEntity aEntidad(Documento documento) {
        return new DocumentoEntity(
                documento.id(),
                documento.entidadTipo().name(),
                documento.entidadId(),
                documento.nombre(),
                documento.tipoContenido().mime(),
                documento.tamano(),
                documento.origen().name(),
                documento.creadoEn());
    }

    public Documento aDominio(DocumentoEntity entidad) {
        return new Documento(
                entidad.getId(),
                EntidadTipo.valueOf(entidad.getEntidadTipo()),
                entidad.getEntidadId(),
                entidad.getNombre(),
                TipoContenido.desdeMime(entidad.getContentType()),
                entidad.getTamano(),
                OrigenDocumento.valueOf(entidad.getOrigen()),
                entidad.getCreadoEn());
    }
}
