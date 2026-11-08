package com.facturacion.factura.internal.domain.repository;

import com.facturacion.factura.internal.domain.model.Factura;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    List<Factura> listar();

    List<Factura> listarPorEmpresaEntreFechas(Long empresaId, LocalDate desde, LocalDate hasta);

    void eliminar(Long id);

    boolean existeEmitidaPorNumero(Long empresaId, String numero);

    boolean existeRecibidaPorNumero(Long empresaId, Long terceroId, String numero);
}
