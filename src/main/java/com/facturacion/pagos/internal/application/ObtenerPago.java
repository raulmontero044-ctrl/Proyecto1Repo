package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.PagoNoEncontradoException;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerPago {

    private final PagoRepository repository;

    public ObtenerPago(PagoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagoDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(PagoDtoConverter::aDto)
                .orElseThrow(() -> new PagoNoEncontradoException(id));
    }
}
