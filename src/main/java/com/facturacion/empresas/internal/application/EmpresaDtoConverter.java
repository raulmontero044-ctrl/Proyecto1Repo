package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.api.facade.dto.NuevaEmpresaDto;
import com.facturacion.empresas.internal.domain.model.Empresa;

final class EmpresaDtoConverter {

    private EmpresaDtoConverter() {
    }

    static Empresa aDominio(NuevaEmpresaDto dto) {
        return Empresa.nueva(
                dto.razonSocial(),
                dto.nif(),
                dto.direccion(),
                dto.codigoPostal(),
                dto.municipio(),
                dto.provincia(),
                dto.pais(),
                dto.email(),
                dto.telefono());
    }

    static EmpresaDto aDto(Empresa empresa) {
        return new EmpresaDto(
                empresa.id(),
                empresa.razonSocial(),
                empresa.nif(),
                empresa.direccion(),
                empresa.codigoPostal(),
                empresa.municipio(),
                empresa.provincia(),
                empresa.pais(),
                empresa.email(),
                empresa.telefono(),
                empresa.activa());
    }
}
