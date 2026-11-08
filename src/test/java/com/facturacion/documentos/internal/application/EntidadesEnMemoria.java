package com.facturacion.documentos.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.pagos.api.facade.PagoFacade;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.terceros.api.facade.TerceroFacade;
import com.facturacion.terceros.api.facade.dto.TerceroDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class EntidadesEnMemoria {

    private final Map<Long, FacturaDto> facturas = new HashMap<>();
    private final Map<Long, EmpresaDto> empresas = new HashMap<>();
    private final Map<Long, TerceroDto> terceros = new HashMap<>();
    private final Map<Long, PagoDto> pagos = new HashMap<>();

    static FacturaDto factura(Long id, String numero, String tipo) {
        return new FacturaDto(
                id,
                numero,
                tipo,
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 12, 3),
                3L,
                "Empresa SL",
                "B00000003",
                "Calle Mayor 1",
                20L,
                "Tercero SA",
                "A00000020",
                "Avenida Diagonal 2",
                new BigDecimal("21.00"),
                List.of(new LineaFacturaDto(
                        "Servicio", new BigDecimal("2.000"), new BigDecimal("50.00"), new BigDecimal("100.00"))),
                new BigDecimal("100.00"),
                new BigDecimal("21.00"),
                new BigDecimal("121.00"));
    }

    static EmpresaDto empresa(Long id) {
        return new EmpresaDto(
                id, "Empresa SL", "B00000003", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null,
                true);
    }

    static TerceroDto tercero(Long id) {
        return new TerceroDto(
                id, 3L, "A00000020", "Tercero SA", "Avenida Diagonal 2", "08001", "Barcelona", "Barcelona",
                "España", null, null, true, false, true);
    }

    static PagoDto pago(Long id) {
        return new PagoDto(
                id, 10L, 3L, "COBRO", LocalDate.of(2026, 11, 3), new BigDecimal("121.00"), "TRANSFERENCIA", null);
    }

    void registrar(FacturaDto factura) {
        facturas.put(factura.id(), factura);
    }

    void registrar(EmpresaDto empresa) {
        empresas.put(empresa.id(), empresa);
    }

    void registrar(TerceroDto tercero) {
        terceros.put(tercero.id(), tercero);
    }

    void registrar(PagoDto pago) {
        pagos.put(pago.id(), pago);
    }

    EmpresaFacade empresaFacade() {
        return id -> Optional.ofNullable(empresas.get(id));
    }

    TerceroFacade terceroFacade() {
        return id -> Optional.ofNullable(terceros.get(id));
    }

    FacturaFacade facturaFacade() {
        return new FacturaFacade() {
            @Override
            public Optional<FacturaDto> buscar(Long id) {
                return Optional.ofNullable(facturas.get(id));
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
            public List<FacturaDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
                throw new UnsupportedOperationException();
            }
        };
    }

    PagoFacade pagoFacade() {
        return new PagoFacade() {
            @Override
            public Optional<PagoDto> buscar(Long id) {
                return Optional.ofNullable(pagos.get(id));
            }

            @Override
            public List<PagoDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<EstadoFacturaDto> estadosPorEmpresa(Long empresaId) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
