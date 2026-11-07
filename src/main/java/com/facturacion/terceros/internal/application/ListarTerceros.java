package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarTerceros {

    private final TerceroRepository repository;

    public ListarTerceros(TerceroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TerceroDto> ejecutar(Long empresaId, String tipo) {
        List<Tercero> terceros = tipo == null
                ? repository.listarPorEmpresa(empresaId)
                : repository.listarPorEmpresaYTipo(empresaId, TerceroDtoConverter.aTipo(tipo));
        return terceros.stream().map(TerceroDtoConverter::aDto).toList();
    }
}
