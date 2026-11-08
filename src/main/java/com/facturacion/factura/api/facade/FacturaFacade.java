package com.facturacion.factura.api.facade;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FacturaFacade {

    FacturaDto crear(NuevaFacturaDto nuevaFactura);

    FacturaDto obtener(Long id);

    List<FacturaDto> listar();

    void eliminar(Long id);

    Optional<FacturaDto> buscar(Long id);

    List<FacturaDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta);
}
