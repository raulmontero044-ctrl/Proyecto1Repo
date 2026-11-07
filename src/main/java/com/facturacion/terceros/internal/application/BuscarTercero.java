package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarTercero {

    private final TerceroRepository repository;

    public BuscarTercero(TerceroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<TerceroDto> ejecutar(Long id) {
        return repository.buscarPorId(id).map(TerceroDtoConverter::aDto);
    }
}
