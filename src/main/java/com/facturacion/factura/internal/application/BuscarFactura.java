package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuscarFactura {

    private final FacturaRepository repository;

    public BuscarFactura(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<FacturaDto> ejecutar(Long id) {
        return repository.buscarPorId(id).map(FacturaDtoConverter::aDto);
    }
}
