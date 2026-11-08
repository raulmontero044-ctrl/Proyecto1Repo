package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaEliminadaEvento;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarFactura {

    private final FacturaRepository repository;
    private final ApplicationEventPublisher publicador;

    public EliminarFactura(FacturaRepository repository, ApplicationEventPublisher publicador) {
        this.repository = repository;
        this.publicador = publicador;
    }

    @Transactional
    public void ejecutar(Long id) {
        if (repository.buscarPorId(id).isEmpty()) {
            throw new FacturaNoEncontradaException(id);
        }
        repository.eliminar(id);
        publicador.publishEvent(new FacturaEliminadaEvento(id));
    }
}
