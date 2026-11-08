package com.facturacion.documentos.internal.persistence.repository;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.persistence.dao.DocumentoContenidoDao;
import com.facturacion.documentos.internal.persistence.dao.DocumentoDao;
import com.facturacion.documentos.internal.persistence.entity.DocumentoContenidoEntity;
import com.facturacion.documentos.internal.persistence.entity.DocumentoEntity;
import com.facturacion.documentos.internal.persistence.mapper.DocumentoMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JpaDocumentoRepositoryTest {

    private static final Instant CREADO_EN = Instant.parse("2026-11-08T11:00:00Z");
    private static final byte[] CONTENIDO = {1, 2, 3};

    private final DocumentoDao dao = mock(DocumentoDao.class);
    private final DocumentoContenidoDao contenidoDao = mock(DocumentoContenidoDao.class);
    private final JpaDocumentoRepository repository =
            new JpaDocumentoRepository(dao, contenidoDao, new DocumentoMapper());

    private static DocumentoEntity entidad(Long id) {
        return new DocumentoEntity(id, "FACTURA", 7L, "doc-" + id + ".pdf", "application/pdf", 3, "ADJUNTO", CREADO_EN);
    }

    @Test
    void guardarPersisteLosMetadatosYDespuesElContenidoConElIdAsignado() {
        when(dao.save(any(DocumentoEntity.class))).thenReturn(entidad(5L));
        Documento nuevo = Documento.nuevo(
                EntidadTipo.FACTURA, 7L, "doc-5.pdf", TipoContenido.PDF, 3, OrigenDocumento.ADJUNTO, CREADO_EN);

        Documento guardado = repository.guardar(nuevo, CONTENIDO);

        ArgumentCaptor<DocumentoEntity> metadatos = ArgumentCaptor.forClass(DocumentoEntity.class);
        ArgumentCaptor<DocumentoContenidoEntity> contenido = ArgumentCaptor.forClass(DocumentoContenidoEntity.class);
        InOrder orden = inOrder(dao, contenidoDao);
        orden.verify(dao).save(metadatos.capture());
        orden.verify(contenidoDao).save(contenido.capture());
        assertThat(metadatos.getValue().getId()).isNull();
        assertThat(metadatos.getValue().getContentType()).isEqualTo("application/pdf");
        assertThat(contenido.getValue().getDocumentoId()).isEqualTo(5L);
        assertThat(contenido.getValue().getContenido()).containsExactly(1, 2, 3);
        assertThat(guardado).isEqualTo(new Documento(
                5L, EntidadTipo.FACTURA, 7L, "doc-5.pdf", TipoContenido.PDF, 3, OrigenDocumento.ADJUNTO, CREADO_EN));
    }

    @Test
    void buscarPorIdSoloLeeLosMetadatos() {
        when(dao.findById(5L)).thenReturn(Optional.of(entidad(5L)));

        assertThat(repository.buscarPorId(5L)).hasValueSatisfying(documento -> {
            assertThat(documento.id()).isEqualTo(5L);
            assertThat(documento.nombre()).isEqualTo("doc-5.pdf");
        });
        verifyNoInteractions(contenidoDao);
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void buscarContenidoDevuelveElBinarioDelDocumento() {
        when(contenidoDao.findById(5L)).thenReturn(Optional.of(new DocumentoContenidoEntity(5L, CONTENIDO)));

        assertThat(repository.buscarContenido(5L)).hasValueSatisfying(
                contenido -> assertThat(contenido).containsExactly(1, 2, 3));
        verifyNoInteractions(dao);
    }

    @Test
    void buscarContenidoInexistenteDevuelveVacio() {
        when(contenidoDao.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarContenido(99L)).isEmpty();
    }

    @Test
    void listarPorEntidadConsultaPorTipoEIdSinCargarElContenido() {
        when(dao.findAllByEntidadTipoAndEntidadIdOrderByIdAsc("FACTURA", 7L))
                .thenReturn(List.of(entidad(1L), entidad(2L)));

        assertThat(repository.listarPorEntidad(EntidadTipo.FACTURA, 7L))
                .extracting(Documento::id)
                .containsExactly(1L, 2L);
        verifyNoInteractions(contenidoDao);
    }

    @Test
    void eliminarBorraElContenidoYLosMetadatos() {
        repository.eliminar(5L);

        InOrder orden = inOrder(contenidoDao, dao);
        orden.verify(contenidoDao).deleteById(5L);
        orden.verify(dao).deleteById(5L);
    }

    @Test
    void eliminarPorEntidadBorraElContenidoYLosMetadatosDeSusDocumentos() {
        List<DocumentoEntity> entidades = List.of(entidad(1L), entidad(2L));
        when(dao.findAllByEntidadTipoAndEntidadIdOrderByIdAsc("FACTURA", 7L)).thenReturn(entidades);

        repository.eliminarPorEntidad(EntidadTipo.FACTURA, 7L);

        InOrder orden = inOrder(contenidoDao, dao);
        orden.verify(contenidoDao).deleteAllById(List.of(1L, 2L));
        orden.verify(dao).deleteAll(entidades);
        verify(dao, never()).deleteById(anyLong());
    }
}
