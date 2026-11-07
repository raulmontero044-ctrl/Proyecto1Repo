package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.DatosFiscalesEmpresaDto;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import com.facturacion.empresas.internal.domain.exception.NifEmpresaDuplicadoException;
import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModificarDatosFiscalesEmpresa {

    private final EmpresaRepository repository;

    public ModificarDatosFiscalesEmpresa(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmpresaDto ejecutar(Long id, DatosFiscalesEmpresaDto datosFiscales) {
        Empresa modificada = repository.buscarPorId(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id))
                .conDatosFiscales(
                        datosFiscales.razonSocial(),
                        datosFiscales.nif(),
                        datosFiscales.direccion(),
                        datosFiscales.codigoPostal(),
                        datosFiscales.municipio(),
                        datosFiscales.provincia(),
                        datosFiscales.pais());
        if (repository.existePorNifEnOtraEmpresa(modificada.nif(), id)) {
            throw new NifEmpresaDuplicadoException();
        }
        return EmpresaDtoConverter.aDto(repository.guardar(modificada));
    }
}
