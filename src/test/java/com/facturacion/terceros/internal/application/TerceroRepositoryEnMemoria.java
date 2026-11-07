package com.facturacion.terceros.internal.application;

import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.model.TipoTercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TerceroRepositoryEnMemoria implements TerceroRepository {

    private final Map<Long, Tercero> terceros = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Tercero guardar(Tercero tercero) {
        Long id = tercero.id() != null ? tercero.id() : siguienteId++;
        Tercero guardado = new Tercero(
                id,
                tercero.empresaId(),
                tercero.nif(),
                tercero.razonSocial(),
                tercero.direccion(),
                tercero.codigoPostal(),
                tercero.municipio(),
                tercero.provincia(),
                tercero.pais(),
                tercero.email(),
                tercero.telefono(),
                tercero.esCliente(),
                tercero.esProveedor(),
                tercero.activo());
        terceros.put(id, guardado);
        return guardado;
    }

    @Override
    public Optional<Tercero> buscarPorId(Long id) {
        return Optional.ofNullable(terceros.get(id));
    }

    @Override
    public List<Tercero> listarPorEmpresa(Long empresaId) {
        return terceros.values().stream().filter(tercero -> tercero.empresaId().equals(empresaId)).toList();
    }

    @Override
    public List<Tercero> listarPorEmpresaYTipo(Long empresaId, TipoTercero tipo) {
        return listarPorEmpresa(empresaId).stream()
                .filter(tercero -> tipo == TipoTercero.CLIENTE ? tercero.esCliente() : tercero.esProveedor())
                .toList();
    }

    @Override
    public boolean existePorEmpresaYNif(Long empresaId, String nif) {
        return terceros.values().stream()
                .anyMatch(tercero -> tercero.empresaId().equals(empresaId) && tercero.nif().equals(nif));
    }

    @Override
    public boolean existePorEmpresaYNifEnOtroTercero(Long empresaId, String nif, Long id) {
        return terceros.values().stream()
                .anyMatch(tercero -> tercero.empresaId().equals(empresaId)
                        && tercero.nif().equals(nif)
                        && !tercero.id().equals(id));
    }
}
