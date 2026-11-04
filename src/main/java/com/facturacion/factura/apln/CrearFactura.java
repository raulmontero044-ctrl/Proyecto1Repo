package com.facturacion.factura.apln;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearFactura {

    private final FacturaRepository repository;

    public CrearFactura(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FacturaDto ejecutar(NuevaFacturaDto nuevaFactura) {
        Factura factura = FacturaDtoConverter.aDominio(nuevaFactura);
        if (repository.existePorNumero(factura.numero())) {
            throw new NumeroFacturaDuplicadoException(factura.numero());
        }
        return FacturaDtoConverter.aDto(repository.guardar(factura));
    }
}
