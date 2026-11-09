package com.facturacion.informes.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.pagos.api.facade.PagoFacade;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FacadesEnMemoria {

    private final List<FacturaDto> facturas = new ArrayList<>();
    private final List<EstadoFacturaDto> estados = new ArrayList<>();
    private final List<PagoDto> pagos = new ArrayList<>();
    private final List<String> consultas = new ArrayList<>();

    void registrar(FacturaDto factura) {
        facturas.add(factura);
    }

    void registrar(EstadoFacturaDto estado) {
        estados.add(estado);
    }

    void registrar(PagoDto pago) {
        pagos.add(pago);
    }

    List<String> consultas() {
        return List.copyOf(consultas);
    }

    FacturaFacade facturaFacade() {
        return new FacturaFacade() {
            @Override
            public List<FacturaDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
                consultas.add("facturas " + empresaId + " " + desde + " " + hasta);
                return facturas.stream()
                        .filter(factura -> factura.empresaId().equals(empresaId))
                        .filter(factura -> !factura.fecha().isBefore(desde) && !factura.fecha().isAfter(hasta))
                        .toList();
            }

            @Override
            public FacturaDto crear(NuevaFacturaDto nuevaFactura) {
                throw new UnsupportedOperationException();
            }

            @Override
            public FacturaDto obtener(Long id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<FacturaDto> listar() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void eliminar(Long id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<FacturaDto> buscar(Long id) {
                throw new UnsupportedOperationException();
            }
        };
    }

    PagoFacade pagoFacade() {
        return new PagoFacade() {
            @Override
            public List<PagoDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
                consultas.add("pagos " + empresaId + " " + desde + " " + hasta);
                return pagos.stream()
                        .filter(pago -> pago.empresaId().equals(empresaId))
                        .filter(pago -> !pago.fecha().isBefore(desde) && !pago.fecha().isAfter(hasta))
                        .toList();
            }

            @Override
            public List<EstadoFacturaDto> estadosPorEmpresa(Long empresaId) {
                consultas.add("estados " + empresaId);
                return List.copyOf(estados);
            }

            @Override
            public Optional<PagoDto> buscar(Long id) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
