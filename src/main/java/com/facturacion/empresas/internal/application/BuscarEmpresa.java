package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarEmpresa {

    private final EmpresaRepository repository;

    public BuscarEmpresa(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<EmpresaDto> ejecutar(Long id) {
        return repository.buscarPorId(id).map(EmpresaDtoConverter::aDto);
    }
}
