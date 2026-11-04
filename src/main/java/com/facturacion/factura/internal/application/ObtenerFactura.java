package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerFactura {

    private final FacturaRepository repository;

    public ObtenerFactura(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public FacturaDto ejecutar(Long id) {
        return repository.buscarPorId(id)
                .map(FacturaDtoConverter::aDto)
                .orElseThrow(() -> new FacturaNoEncontradaException(id));
    }
}
