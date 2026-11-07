package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.api.facade.dto.NuevaEmpresaDto;
import com.facturacion.empresas.internal.domain.exception.NifEmpresaDuplicadoException;
import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearEmpresa {

    private final EmpresaRepository repository;

    public CrearEmpresa(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmpresaDto ejecutar(NuevaEmpresaDto nuevaEmpresa) {
        Empresa empresa = EmpresaDtoConverter.aDominio(nuevaEmpresa);
        if (repository.existePorNif(empresa.nif())) {
            throw new NifEmpresaDuplicadoException();
        }
        return EmpresaDtoConverter.aDto(repository.guardar(empresa));
    }
}
