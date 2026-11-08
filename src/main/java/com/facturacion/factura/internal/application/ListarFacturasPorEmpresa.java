package com.facturacion.factura.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.internal.domain.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListarFacturasPorEmpresa {

    private final FacturaRepository repository;

    public ListarFacturasPorEmpresa(FacturaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FacturaDto> ejecutar(Long empresaId, LocalDate desde, LocalDate hasta) {
        return repository.listarPorEmpresaEntreFechas(empresaId, desde, hasta).stream()
                .map(FacturaDtoConverter::aDto)
                .toList();
    }
}
