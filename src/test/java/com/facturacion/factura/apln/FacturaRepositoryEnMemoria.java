package com.facturacion.factura.apln;

import com.facturacion.factura.domain.model.Factura;
import com.facturacion.factura.domain.repository.FacturaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                factura.fecha(),
                factura.clienteNombre(),
                factura.clienteNif(),
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
    public boolean existePorNumero(String numero) {
        return facturas.values().stream().anyMatch(factura -> factura.numero().equals(numero));
    }
}
