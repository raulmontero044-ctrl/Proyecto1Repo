package com.facturacion.factura.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.terceros.api.facade.TerceroFacade;
import com.facturacion.terceros.api.facade.dto.TerceroDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class EmpresasYTercerosEnMemoria {

    static final Long EMPRESA = 1L;
    static final Long CLIENTE = 10L;
    static final Long PROVEEDOR = 20L;

    private final Map<Long, EmpresaDto> empresas = new HashMap<>();
    private final Map<Long, TerceroDto> terceros = new HashMap<>();

    EmpresasYTercerosEnMemoria() {
        registrar(empresa(EMPRESA, true));
        registrar(tercero(CLIENTE, EMPRESA, true, false, true));
        registrar(tercero(PROVEEDOR, EMPRESA, false, true, true));
    }

    static EmpresaDto empresa(Long id, boolean activa) {
        return new EmpresaDto(
                id,
                "Empresa " + id + " SL",
                "B0000000" + id,
                "Calle Mayor 1",
                "28001",
                "Madrid",
                "Madrid",
                "España",
                "empresa@example.com",
                "910000000",
                activa);
    }

    static TerceroDto tercero(Long id, Long empresaId, boolean esCliente, boolean esProveedor, boolean activo) {
        return new TerceroDto(
                id,
                empresaId,
                "A0000000" + id,
                "Tercero " + id + " SA",
                "Avenida Diagonal 2",
                "08001",
                "Barcelona",
                "Barcelona",
                "España",
                null,
                null,
                esCliente,
                esProveedor,
                activo);
    }

    void registrar(EmpresaDto empresa) {
        empresas.put(empresa.id(), empresa);
    }

    void registrar(TerceroDto tercero) {
        terceros.put(tercero.id(), tercero);
    }

    EmpresaFacade empresaFacade() {
        return id -> Optional.ofNullable(empresas.get(id));
    }

    TerceroFacade terceroFacade() {
        return id -> Optional.ofNullable(terceros.get(id));
    }
}
