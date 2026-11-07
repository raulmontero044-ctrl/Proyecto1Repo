package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesactivarTercero {

    private final TerceroRepository repository;

    public DesactivarTercero(TerceroRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ejecutar(Long id) {
        Tercero desactivado = repository.buscarPorId(id)
                .orElseThrow(() -> new TerceroNoEncontradoException(id))
                .desactivado();
        repository.guardar(desactivado);
    }
}
