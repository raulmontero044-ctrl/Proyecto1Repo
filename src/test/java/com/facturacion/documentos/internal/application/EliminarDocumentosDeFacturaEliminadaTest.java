package com.facturacion.documentos.internal.application;

import com.facturacion.documentos.internal.domain.model.Documento;
import com.facturacion.documentos.internal.domain.model.EntidadTipo;
import com.facturacion.documentos.internal.domain.model.OrigenDocumento;
import com.facturacion.documentos.internal.domain.model.TipoContenido;
import com.facturacion.documentos.internal.domain.repository.DocumentoRepository;
import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EliminarDocumentosDeFacturaEliminadaTest {

    private final DocumentoRepositoryEnMemoria repository = new DocumentoRepositoryEnMemoria();

    private Documento guardado(EntidadTipo tipo, Long entidadId) {
        return repository.guardar(
                Documento.nuevo(tipo, entidadId, "a.pdf", TipoContenido.PDF, 1, OrigenDocumento.GENERADO,
                        Instant.parse("2026-11-08T11:00:00Z")),
                new byte[] {1});
    }

    @Test
    void borraLosDocumentosDeLaFacturaEliminadaYConservaLosDemas() {
        guardado(EntidadTipo.FACTURA, 10L);
        guardado(EntidadTipo.FACTURA, 10L);
        Long deOtraFactura = guardado(EntidadTipo.FACTURA, 11L).id();
        Long deUnPagoConElMismoId = guardado(EntidadTipo.PAGO, 10L).id();

        new EliminarDocumentosDeFacturaEliminada(repository).alEliminarFactura(new FacturaEliminadaEvento(10L));

        assertThat(repository.todos()).extracting(Documento::id).containsExactly(deOtraFactura, deUnPagoConElMismoId);
        assertThat(repository.contenidos()).containsOnlyKeys(deOtraFactura, deUnPagoConElMismoId);
    }

    @Test
    void unaFacturaSinDocumentosNoBorraNada() {
        guardado(EntidadTipo.FACTURA, 11L);

        new EliminarDocumentosDeFacturaEliminada(repository).alEliminarFactura(new FacturaEliminadaEvento(10L));

        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void escuchaElEventoDeFormaSincronaAlPublicarlo() {
        guardado(EntidadTipo.FACTURA, 10L);
        try (AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext()) {
            contexto.registerBean(DocumentoRepository.class, () -> repository);
            contexto.register(EliminarDocumentosDeFacturaEliminada.class);
            contexto.refresh();

            contexto.publishEvent(new FacturaEliminadaEvento(10L));
        }

        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void esUnEventListenerYNoEsperaAlCommit() throws NoSuchMethodException {
        Method metodo = EliminarDocumentosDeFacturaEliminada.class
                .getMethod("alEliminarFactura", FacturaEliminadaEvento.class);

        assertThat(metodo.isAnnotationPresent(EventListener.class)).isTrue();
        assertThat(metodo.isAnnotationPresent(TransactionalEventListener.class)).isFalse();
    }
}
