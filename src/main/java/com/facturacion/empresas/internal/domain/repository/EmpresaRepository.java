package com.facturacion.empresas.internal.domain.repository;

import com.facturacion.empresas.internal.domain.model.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {

    Empresa guardar(Empresa empresa);

    Optional<Empresa> buscarPorId(Long id);

    List<Empresa> listar();

    boolean existePorNif(String nif);

    boolean existePorNifEnOtraEmpresa(String nif, Long id);
}
