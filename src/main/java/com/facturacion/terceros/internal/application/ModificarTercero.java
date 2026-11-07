package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.api.facade.dto.DatosTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.exception.NifTerceroDuplicadoException;
import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModificarTercero {

    private final TerceroRepository repository;

    public ModificarTercero(TerceroRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TerceroDto ejecutar(Long id, DatosTerceroDto datos) {
        Tercero modificado = repository.buscarPorId(id)
                .orElseThrow(() -> new TerceroNoEncontradoException(id))
                .conDatos(
                        datos.nif(),
                        datos.razonSocial(),
                        datos.direccion(),
                        datos.codigoPostal(),
                        datos.municipio(),
                        datos.provincia(),
                        datos.pais(),
                        datos.email(),
                        datos.telefono(),
                        datos.esCliente(),
                        datos.esProveedor());
        if (repository.existePorEmpresaYNifEnOtroTercero(modificado.empresaId(), modificado.nif(), id)) {
            throw new NifTerceroDuplicadoException();
        }
        return TerceroDtoConverter.aDto(repository.guardar(modificado));
    }
}
