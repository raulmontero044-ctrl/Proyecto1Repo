package com.facturacion.factura.internal.application;

import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class FacturaRepositoryEnMemoria implements FacturaRepository {

    private final Map<Long, Factura> facturas = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Factura guardar(Factura factura) {
        Long id = factura.id() != null ? factura.id() : siguienteId++;
        Factura guardada = new Factura(
                id,
                factura.numero(),
                factura.tipo(),
                factura.fecha(),
                factura.fechaVencimiento(),
                factura.empresaId(),
                factura.empresaRazonSocial(),
                factura.empresaNif(),
                factura.empresaDireccion(),
                factura.terceroId(),
                factura.terceroNombre(),
                factura.terceroNif(),
                factura.terceroDireccion(),
                factura.porcentajeIva(),
                factura.lineas());
        facturas.put(id, guardada);
        return guardada;
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return Optional.ofNullable(facturas.get(id));
    }

    @Override
    public List<Factura> listar() {
        return new ArrayList<>(facturas.values());
    }

    @Override
    public void eliminar(Long id) {
        facturas.remove(id);
    }

    @Override
    public boolean existeEmitidaPorNumero(Long empresaId, String numero) {
        return facturas.values().stream().anyMatch(factura -> factura.tipo() == TipoFactura.EMITIDA
                && Objects.equals(factura.empresaId(), empresaId)
                && factura.numero().equals(numero));
    }

    @Override
    public boolean existeRecibidaPorNumero(Long empresaId, Long terceroId, String numero) {
        return facturas.values().stream().anyMatch(factura -> factura.tipo() == TipoFactura.RECIBIDA
                && Objects.equals(factura.empresaId(), empresaId)
                && Objects.equals(factura.terceroId(), terceroId)
                && factura.numero().equals(numero));
    }
}
