package com.facturacion.terceros.api.facade;

import com.facturacion.terceros.api.facade.dto.TerceroDto;

import java.util.Optional;

public interface TerceroFacade {

    Optional<TerceroDto> buscar(Long id);
}
