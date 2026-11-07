package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerEmpresa {

    private final EmpresaRepository repository;

    public ObtenerEmpresa(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public EmpresaDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(EmpresaDtoConverter::aDto)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id));
    }
}
