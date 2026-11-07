package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class EmpresaRepositoryEnMemoria implements EmpresaRepository {

    private final Map<Long, Empresa> empresas = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Empresa guardar(Empresa empresa) {
        Long id = empresa.id() != null ? empresa.id() : siguienteId++;
        Empresa guardada = new Empresa(
                id,
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
        empresas.put(id, guardada);
        return guardada;
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        return Optional.ofNullable(empresas.get(id));
    }

    @Override
    public List<Empresa> listar() {
        return new ArrayList<>(empresas.values());
    }

    @Override
    public boolean existePorNif(String nif) {
        return empresas.values().stream().anyMatch(empresa -> empresa.nif().equals(nif));
    }

    @Override
    public boolean existePorNifEnOtraEmpresa(String nif, Long id) {
        return empresas.values().stream()
                .anyMatch(empresa -> empresa.nif().equals(nif) && !empresa.id().equals(id));
    }
}
