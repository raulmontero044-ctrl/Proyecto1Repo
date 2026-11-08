package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.FacturaFacade;
import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.pagos.api.facade.dto.NuevoPagoDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import com.facturacion.pagos.internal.domain.service.CalculadoraEstadoFactura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
public class RegistrarPago {

    private final PagoRepository repository;
    private final FacturaFacade facturaFacade;
    private final Clock reloj;

    public RegistrarPago(PagoRepository repository, FacturaFacade facturaFacade, Clock reloj) {
        this.repository = repository;
        this.facturaFacade = facturaFacade;
        this.reloj = reloj;
    }

    @Transactional
    public PagoDto ejecutar(NuevoPagoDto nuevoPago) {
        FacturaDto factura = facturaExistente(nuevoPago.facturaId());
        Pago pago = Pago.nuevo(
                factura.id(),
                factura.empresaId(),
                TipoPago.paraFacturaDeTipo(factura.tipo()),
                nuevoPago.fecha(),
                nuevoPago.importe(),
                PagoDtoConverter.aMetodo(nuevoPago.metodo()),
                nuevoPago.referencia(),
                LocalDate.now(reloj));
        BigDecimal pagado = CalculadoraEstadoFactura.pagado(repository.listarPorFactura(factura.id()));
        CalculadoraEstadoFactura.exigirQueNoSupereElTotal(factura.id(), factura.total(), pagado, pago.importe());
        return PagoDtoConverter.aDto(repository.guardar(pago));
    }

    private FacturaDto facturaExistente(Long facturaId) {
        if (facturaId == null) {
            throw new PagoInvalidoException("La factura del pago es obligatoria");
        }
        return facturaFacade.buscar(facturaId)
                .orElseThrow(() -> new PagoInvalidoException("No existe la factura con id " + facturaId));
    }
}
