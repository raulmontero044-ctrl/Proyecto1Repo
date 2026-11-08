package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListarPagosPorEmpresa {

    private final PagoRepository repository;

    public ListarPagosPorEmpresa(PagoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PagoDto> ejecutar(Long empresaId, LocalDate desde, LocalDate hasta) {
        return repository.listarPorEmpresaEntreFechas(empresaId, desde, hasta).stream()
                .map(PagoDtoConverter::aDto)
                .toList();
    }
}
