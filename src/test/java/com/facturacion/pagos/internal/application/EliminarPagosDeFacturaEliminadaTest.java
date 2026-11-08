package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EliminarPagosDeFacturaEliminadaTest {

    private final PagoRepositoryEnMemoria repository = new PagoRepositoryEnMemoria();

    private Pago guardado(Long facturaId) {
        return repository.guardar(new Pago(
                null, facturaId, 3L, TipoPago.COBRO, LocalDate.of(2026, 11, 3), BigDecimal.TEN,
                MetodoPago.EFECTIVO, null));
    }

    @Test
    void borraTodosLosPagosDeLaFacturaEliminadaYConservaLosDemas() {
        guardado(10L);
        guardado(10L);
        Long ajeno = guardado(11L).id();

        new EliminarPagosDeFacturaEliminada(repository).alEliminarFactura(new FacturaEliminadaEvento(10L));

        assertThat(repository.todos()).extracting(Pago::id).containsExactly(ajeno);
    }

    @Test
    void unaFacturaSinPagosNoBorraNada() {
        guardado(11L);

        new EliminarPagosDeFacturaEliminada(repository).alEliminarFactura(new FacturaEliminadaEvento(10L));

        assertThat(repository.todos()).hasSize(1);
    }

    @Test
    void escuchaElEventoDeFormaSincronaAlPublicarlo() {
        guardado(10L);
        try (AnnotationConfigApplicationContext contexto = new AnnotationConfigApplicationContext()) {
            contexto.registerBean(PagoRepository.class, () -> repository);
            contexto.register(EliminarPagosDeFacturaEliminada.class);
            contexto.refresh();

            contexto.publishEvent(new FacturaEliminadaEvento(10L));
        }

        assertThat(repository.todos()).isEmpty();
    }

    @Test
    void esUnEventListenerYNoEsperaAlCommit() throws NoSuchMethodException {
        Method metodo = EliminarPagosDeFacturaEliminada.class
                .getMethod("alEliminarFactura", FacturaEliminadaEvento.class);

        assertThat(metodo.isAnnotationPresent(EventListener.class)).isTrue();
        assertThat(metodo.isAnnotationPresent(TransactionalEventListener.class)).isFalse();
    }
}
