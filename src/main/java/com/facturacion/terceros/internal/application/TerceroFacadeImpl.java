package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.TerceroFacade;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TerceroFacadeImpl implements TerceroFacade {

    private final BuscarTercero buscarTercero;

    TerceroFacadeImpl(BuscarTercero buscarTercero) {
        this.buscarTercero = buscarTercero;
    }

    @Override
    public Optional<TerceroDto> buscar(Long id) {
        return buscarTercero.ejecutar(id);
    }
}
