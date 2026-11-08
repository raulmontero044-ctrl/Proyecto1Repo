package com.facturacion.pagos.internal.domain.repository;

import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagoRepository {

    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(Long id);

    List<Pago> listarPorFactura(Long facturaId);

    List<Pago> listarPorEmpresa(Long empresaId);

    List<Pago> listarPorEmpresaYTipo(Long empresaId, TipoPago tipo);

    List<Pago> listarPorEmpresaEntreFechas(Long empresaId, LocalDate desde, LocalDate hasta);

    void eliminar(Long id);

    void eliminarPorFactura(Long facturaId);
}
