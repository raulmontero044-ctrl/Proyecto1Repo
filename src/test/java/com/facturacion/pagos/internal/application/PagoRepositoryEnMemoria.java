package com.facturacion.pagos.internal.application;

import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

class PagoRepositoryEnMemoria implements PagoRepository {

    private static final Comparator<Pago> POR_FECHA_E_ID =
            Comparator.comparing(Pago::fecha).thenComparing(Pago::id);

    private final Map<Long, Pago> pagos = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Pago guardar(Pago pago) {
        Long id = pago.id() != null ? pago.id() : siguienteId++;
        Pago guardado = new Pago(
                id,
                pago.facturaId(),
                pago.empresaId(),
                pago.tipo(),
                pago.fecha(),
                pago.importe(),
                pago.metodo(),
                pago.referencia());
        pagos.put(id, guardado);
        return guardado;
    }

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        return Optional.ofNullable(pagos.get(id));
    }

    @Override
    public List<Pago> listarPorFactura(Long facturaId) {
        return filtrar(pago -> pago.facturaId().equals(facturaId));
    }

    @Override
    public List<Pago> listarPorEmpresa(Long empresaId) {
        return filtrar(pago -> pago.empresaId().equals(empresaId));
    }

    @Override
    public List<Pago> listarPorEmpresaYTipo(Long empresaId, TipoPago tipo) {
        return filtrar(pago -> pago.empresaId().equals(empresaId) && pago.tipo() == tipo);
    }

    @Override
    public List<Pago> listarPorEmpresaEntreFechas(Long empresaId, LocalDate desde, LocalDate hasta) {
        return filtrar(pago -> pago.empresaId().equals(empresaId)
                && !pago.fecha().isBefore(desde)
                && !pago.fecha().isAfter(hasta));
    }

    @Override
    public void eliminar(Long id) {
        pagos.remove(id);
    }

    @Override
    public void eliminarPorFactura(Long facturaId) {
        pagos.values().removeIf(pago -> pago.facturaId().equals(facturaId));
    }

    List<Pago> todos() {
        return List.copyOf(pagos.values());
    }

    private List<Pago> filtrar(Predicate<Pago> criterio) {
        return pagos.values().stream().filter(criterio).sorted(POR_FECHA_E_ID).toList();
    }
}
