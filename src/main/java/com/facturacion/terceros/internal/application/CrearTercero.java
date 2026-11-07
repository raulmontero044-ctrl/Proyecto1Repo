package com.facturacion.terceros.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.terceros.api.facade.dto.NuevoTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.exception.EmpresaDelTerceroInexistenteException;
import com.facturacion.terceros.internal.domain.exception.NifTerceroDuplicadoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearTercero {

    private final TerceroRepository repository;
    private final EmpresaFacade empresaFacade;

    public CrearTercero(TerceroRepository repository, EmpresaFacade empresaFacade) {
        this.repository = repository;
        this.empresaFacade = empresaFacade;
    }

    @Transactional
    public TerceroDto ejecutar(NuevoTerceroDto nuevoTercero) {
        Tercero tercero = TerceroDtoConverter.aDominio(nuevoTercero);
        if (empresaFacade.buscar(tercero.empresaId()).isEmpty()) {
            throw new EmpresaDelTerceroInexistenteException(tercero.empresaId());
        }
        if (repository.existePorEmpresaYNif(tercero.empresaId(), tercero.nif())) {
            throw new NifTerceroDuplicadoException();
        }
        return TerceroDtoConverter.aDto(repository.guardar(tercero));
    }
}
