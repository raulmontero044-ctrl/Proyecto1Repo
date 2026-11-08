package com.facturacion.documentos.internal.persistence.mapper;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.persistence.entity.DocumentoEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoMapperTest {

    private static final Instant CREADO_EN = Instant.parse("2026-11-08T11:00:00.123456Z");

    private final DocumentoMapper mapper = new DocumentoMapper();

    @Test
    void idaYVueltaConservaTodosLosCampos() {
        Documento documento = new Documento(
                5L, EntidadTipo.TERCERO, 20L, "contrato.pdf", TipoContenido.PDF, 2048, OrigenDocumento.ADJUNTO,
                CREADO_EN);

        assertThat(mapper.aDominio(mapper.aEntidad(documento))).isEqualTo(documento);
    }

    @Test
    void aEntidadGuardaLosEnumeradosComoTextoYElTipoDeContenidoComoMime() {
        Documento documento = Documento.nuevo(
                EntidadTipo.PAGO, 9L, "recibo.jpg", TipoContenido.JPEG, 10, OrigenDocumento.GENERADO, CREADO_EN);

        DocumentoEntity entidad = mapper.aEntidad(documento);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getEntidadTipo()).isEqualTo("PAGO");
        assertThat(entidad.getEntidadId()).isEqualTo(9L);
        assertThat(entidad.getNombre()).isEqualTo("recibo.jpg");
        assertThat(entidad.getContentType()).isEqualTo("image/jpeg");
        assertThat(entidad.getTamano()).isEqualTo(10);
        assertThat(entidad.getOrigen()).isEqualTo("GENERADO");
        assertThat(entidad.getCreadoEn()).isEqualTo(CREADO_EN);
    }

    @Test
    void aDominioReconstruyeElModeloDesdeLaEntidad() {
        DocumentoEntity entidad = new DocumentoEntity(
                3L, "EMPRESA", 4L, "logo.png", "image/png", 512, "ADJUNTO", CREADO_EN);

        assertThat(mapper.aDominio(entidad)).isEqualTo(new Documento(
                3L, EntidadTipo.EMPRESA, 4L, "logo.png", TipoContenido.PNG, 512, OrigenDocumento.ADJUNTO, CREADO_EN));
    }
}
