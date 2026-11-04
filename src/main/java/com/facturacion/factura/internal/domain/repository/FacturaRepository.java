package com.facturacion.factura.internal.domain.repository;

import com.facturacion.factura.internal.domain.model.Factura;

import java.util.List;
import java.util.Optional;

public interface FacturaRepository {

    Factura guardar(Factura factura);

    Optional<Factura> buscarPorId(Long id);

    List<Factura> listar();

    void eliminar(Long id);

    boolean existePorNumero(String numero);
}
