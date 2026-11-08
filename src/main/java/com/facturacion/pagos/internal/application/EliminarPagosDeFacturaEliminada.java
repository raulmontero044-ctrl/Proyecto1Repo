package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EliminarPagosDeFacturaEliminada {

    private final PagoRepository repository;

    public EliminarPagosDeFacturaEliminada(PagoRepository repository) {
        this.repository = repository;
    }

    @EventListener
    @Transactional
    public void alEliminarFactura(FacturaEliminadaEvento evento) {
        repository.eliminarPorFactura(evento.facturaId());
    }
}
