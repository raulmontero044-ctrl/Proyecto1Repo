package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class EmpresaFacadeImpl implements EmpresaFacade {

    private final BuscarEmpresa buscarEmpresa;

    EmpresaFacadeImpl(BuscarEmpresa buscarEmpresa) {
        this.buscarEmpresa = buscarEmpresa;
    }

    @Override
    public Optional<EmpresaDto> buscar(Long id) {
        return buscarEmpresa.ejecutar(id);
    }
}
