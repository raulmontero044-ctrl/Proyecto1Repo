package com.facturacion.factura.apln;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarFacturas {

    private final FacturaRepository repository;

    public ListarFacturas(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FacturaDto> ejecutar() {
        return repository.listar().stream().map(FacturaDtoConverter::aDto).toList();
    }
}
