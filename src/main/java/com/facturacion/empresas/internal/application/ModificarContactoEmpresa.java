package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.ContactoEmpresaDto;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModificarContactoEmpresa {

    private final EmpresaRepository repository;

    public ModificarContactoEmpresa(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmpresaDto ejecutar(Long id, ContactoEmpresaDto contacto) {
        Empresa modificada = repository.buscarPorId(id)
                .orElseThrow(() -> new EmpresaNoEncontradaException(id))
                .conContacto(contacto.email(), contacto.telefono());
        return EmpresaDtoConverter.aDto(repository.guardar(modificada));
    }
}
