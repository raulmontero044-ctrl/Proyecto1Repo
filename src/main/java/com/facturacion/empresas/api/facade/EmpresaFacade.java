package com.facturacion.empresas.api.facade;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;

import java.util.Optional;

public interface EmpresaFacade {

    Optional<EmpresaDto> buscar(Long id);
}
