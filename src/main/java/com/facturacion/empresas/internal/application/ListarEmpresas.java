package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarEmpresas {

    private final EmpresaRepository repository;

    public ListarEmpresas(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaDto> ejecutar() {
        return repository.listar().stream().map(EmpresaDtoConverter::aDto).toList();
    }
}
