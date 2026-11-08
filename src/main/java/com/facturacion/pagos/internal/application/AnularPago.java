package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.internal.domain.exception.PagoNoEncontradoException;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnularPago {

    private final PagoRepository repository;

    public AnularPago(PagoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id) {
        if (repository.buscarPorId(id).isEmpty()) {
            throw new PagoNoEncontradoException(id);
        }
        repository.eliminar(id);
    }
}
