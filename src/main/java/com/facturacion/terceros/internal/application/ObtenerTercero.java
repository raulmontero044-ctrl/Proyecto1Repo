package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerTercero {

    private final TerceroRepository repository;

    public ObtenerTercero(TerceroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public TerceroDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(TerceroDtoConverter::aDto)
                .orElseThrow(() -> new TerceroNoEncontradoException(id));
    }
}
