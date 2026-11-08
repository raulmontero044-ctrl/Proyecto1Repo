package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.api.facade.PagoFacade;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
class PagoFacadeImpl implements PagoFacade {

    private final BuscarPago buscarPago;
    private final ListarPagosPorEmpresa listarPagosPorEmpresa;
    private final ConsultarEstadoFacturas consultarEstadoFacturas;

    PagoFacadeImpl(
            BuscarPago buscarPago,
            ListarPagosPorEmpresa listarPagosPorEmpresa,
            ConsultarEstadoFacturas consultarEstadoFacturas) {
        this.buscarPago = buscarPago;
        this.listarPagosPorEmpresa = listarPagosPorEmpresa;
        this.consultarEstadoFacturas = consultarEstadoFacturas;
    }

    @Override
    public Optional<PagoDto> buscar(Long id) {
        return buscarPago.ejecutar(id);
    }

    @Override
    public List<PagoDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
        return listarPagosPorEmpresa.ejecutar(empresaId, desde, hasta);
    }

    @Override
    public List<EstadoFacturaDto> estadosPorEmpresa(Long empresaId) {
        return consultarEstadoFacturas.ejecutar(empresaId, null);
    }
}
