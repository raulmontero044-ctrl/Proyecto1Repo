package com.facturacion.factura.apln;

import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarFactura {

    private final FacturaRepository repository;

    public EliminarFactura(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id) {
        if (repository.buscarPorId(id).isEmpty()) {
            throw new FacturaNoEncontradaException(id);
        }
        repository.eliminar(id);
    }
}
