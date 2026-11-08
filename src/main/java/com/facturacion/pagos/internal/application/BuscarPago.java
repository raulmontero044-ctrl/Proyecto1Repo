package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarPago {

    private final PagoRepository repository;

    public BuscarPago(PagoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<PagoDto> ejecutar(Long id) {
        return repository.buscarPorId(id).map(PagoDtoConverter::aDto);
    }
}
