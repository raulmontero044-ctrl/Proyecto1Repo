package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.EstadoFactura;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.SituacionFactura;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import com.facturacion.pagos.internal.domain.service.CalculadoraEstadoFactura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ConsultarEstadoFacturas {

    static final LocalDate PRIMER_DIA = LocalDate.of(1000, 1, 1);
    static final LocalDate ULTIMO_DIA = LocalDate.of(9999, 12, 31);
    private static final String VENCIDA = "VENCIDA";

    private final PagoRepository repository;
    private final FacturaFacade facturaFacade;
    private final Clock reloj;

    public ConsultarEstadoFacturas(PagoRepository repository, FacturaFacade facturaFacade, Clock reloj) {
        this.repository = repository;
        this.facturaFacade = facturaFacade;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<EstadoFacturaDto> ejecutar(Long empresaId, String estado) {
        if (empresaId == null) {
            throw new PagoInvalidoException("La empresa es obligatoria para consultar el estado de las facturas");
        }
        Predicate<SituacionFactura> filtro = filtro(estado);
        LocalDate hoy = LocalDate.now(reloj);
        Map<Long, List<Pago>> pagosPorFactura = repository.listarPorEmpresa(empresaId).stream()
                .collect(Collectors.groupingBy(Pago::facturaId));
        return facturaFacade.listarPorEmpresa(empresaId, PRIMER_DIA, ULTIMO_DIA).stream()
                .map(factura -> new FacturaConSituacion(
                        factura, situacion(factura, pagosPorFactura.getOrDefault(factura.id(), List.of()), hoy)))
                .filter(facturaConSituacion -> filtro.test(facturaConSituacion.situacion()))
                .map(facturaConSituacion -> PagoDtoConverter.aDto(
                        facturaConSituacion.factura(), facturaConSituacion.situacion()))
                .toList();
    }

    private static SituacionFactura situacion(FacturaDto factura, List<Pago> pagos, LocalDate hoy) {
        return CalculadoraEstadoFactura.situacion(
                factura.total(), CalculadoraEstadoFactura.pagado(pagos), factura.fechaVencimiento(), hoy);
    }

    private static Predicate<SituacionFactura> filtro(String estado) {
        if (estado == null) {
            return situacion -> true;
        }
        if (VENCIDA.equals(estado)) {
            return SituacionFactura::vencida;
        }
        EstadoFactura buscado = PagoDtoConverter.aEstado(estado);
        return situacion -> situacion.estado() == buscado;
    }

    private record FacturaConSituacion(FacturaDto factura, SituacionFactura situacion) {
    }
}
