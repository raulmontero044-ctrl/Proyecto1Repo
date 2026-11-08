package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FacturasEnMemoria implements FacturaFacade {

    private final Map<Long, FacturaDto> facturas = new LinkedHashMap<>();
    private final List<List<LocalDate>> rangosConsultados = new ArrayList<>();

    static FacturaDto factura(
            Long id, String tipo, Long empresaId, LocalDate fecha, LocalDate fechaVencimiento, String total) {
        return new FacturaDto(
                id,
                "F-" + id,
                tipo,
                fecha,
                fechaVencimiento,
                empresaId,
                "Empresa SL",
                "B00000001",
                null,
                20L,
                "Tercero SA",
                "A00000020",
                null,
                new BigDecimal("21.00"),
                List.of(),
                new BigDecimal(total),
                BigDecimal.ZERO,
                new BigDecimal(total));
    }

    void registrar(FacturaDto factura) {
        facturas.put(factura.id(), factura);
    }

    List<List<LocalDate>> rangosConsultados() {
        return rangosConsultados;
    }

    @Override
    public Optional<FacturaDto> buscar(Long id) {
        return Optional.ofNullable(facturas.get(id));
    }

    @Override
    public List<FacturaDto> listarPorEmpresa(Long empresaId, LocalDate desde, LocalDate hasta) {
        rangosConsultados.add(List.of(desde, hasta));
        return facturas.values().stream()
                .filter(factura -> empresaId.equals(factura.empresaId()))
                .filter(factura -> !factura.fecha().isBefore(desde) && !factura.fecha().isAfter(hasta))
                .sorted(Comparator.comparing(FacturaDto::fecha).thenComparing(FacturaDto::id))
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
}
