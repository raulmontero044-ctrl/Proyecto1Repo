package com.facturacion.pagos.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.EstadoFactura;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.SituacionFactura;
import com.facturacion.pagos.internal.domain.model.TipoPago;

final class PagoDtoConverter {

    private PagoDtoConverter() {
    }

    static PagoDto aDto(Pago pago) {
        return new PagoDto(
                pago.id(),
                pago.facturaId(),
                pago.empresaId(),
                pago.tipo().name(),
                pago.fecha(),
                pago.importe(),
                pago.metodo().name(),
                pago.referencia());
    }

    static EstadoFacturaDto aDto(FacturaDto factura, SituacionFactura situacion) {
        return new EstadoFacturaDto(
                factura.id(),
                factura.numero(),
                factura.tipo(),
                factura.fecha(),
                factura.fechaVencimiento(),
                situacion.total(),
                situacion.pagado(),
                situacion.pendiente(),
                situacion.estado().name(),
                situacion.vencida());
    }

    static MetodoPago aMetodo(String metodo) {
        if (metodo == null) {
            throw new PagoInvalidoException("El método del pago es obligatorio");
        }
        try {
            return MetodoPago.valueOf(metodo);
        } catch (IllegalArgumentException excepcion) {
            throw new PagoInvalidoException(
                    "El método del pago debe ser TRANSFERENCIA, TARJETA, EFECTIVO, DOMICILIACION u OTRO");
        }
    }

    static TipoPago aTipo(String tipo) {
        try {
            return TipoPago.valueOf(tipo);
        } catch (IllegalArgumentException excepcion) {
            throw new PagoInvalidoException("El tipo del pago debe ser COBRO o PAGO");
        }
    }

    static EstadoFactura aEstado(String estado) {
        try {
            return EstadoFactura.valueOf(estado);
        } catch (IllegalArgumentException excepcion) {
            throw new PagoInvalidoException("El estado debe ser PENDIENTE, PARCIAL, PAGADA o VENCIDA");
        }
    }
}
