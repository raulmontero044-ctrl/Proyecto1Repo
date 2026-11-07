package com.facturacion.terceros.internal.domain.repository;

import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.model.TipoTercero;

import java.util.List;
import java.util.Optional;

public interface TerceroRepository {

    Tercero guardar(Tercero tercero);

    Optional<Tercero> buscarPorId(Long id);

    List<Tercero> listarPorEmpresa(Long empresaId);

    List<Tercero> listarPorEmpresaYTipo(Long empresaId, TipoTercero tipo);

    boolean existePorEmpresaYNif(Long empresaId, String nif);

    boolean existePorEmpresaYNifEnOtroTercero(Long empresaId, String nif, Long id);
}
