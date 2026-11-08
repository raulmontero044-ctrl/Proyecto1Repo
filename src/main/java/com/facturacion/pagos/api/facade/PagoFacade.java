package com.facturacion.pagos.api.facade;

import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PagoFacade {

    Optional<PagoDto> buscar(Long id);

    List<PagoDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta);

    List<EstadoFacturaDto> estadosPorEmpresa(Long empresaId);
}
