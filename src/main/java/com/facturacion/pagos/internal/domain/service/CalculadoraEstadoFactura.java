package com.facturacion.pagos.internal.domain.service;

import com.facturacion.pagos.internal.domain.exception.ImportePagoExcedidoException;
import com.facturacion.pagos.internal.domain.model.EstadoFactura;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.SituacionFactura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class CalculadoraEstadoFactura {

    private static final int ESCALA = 2;

    private CalculadoraEstadoFactura() {
    }

    public static BigDecimal pagado(List<Pago> pagos) {
        return pagos.stream()
                .map(Pago::importe)
                .reduce(BigDecimal.ZERO.setScale(ESCALA), BigDecimal::add);
    }

    public static SituacionFactura situacion(
            BigDecimal total, BigDecimal pagado, LocalDate fechaVencimiento, LocalDate hoy) {
        EstadoFactura estado = estado(total, pagado);
        boolean vencida = estado != EstadoFactura.PAGADA
                && fechaVencimiento != null
                && fechaVencimiento.isBefore(hoy);
        return new SituacionFactura(total, pagado, total.subtract(pagado), estado, vencida);
    }

    public static void exigirQueNoSupereElTotal(
            Long facturaId, BigDecimal total, BigDecimal pagado, BigDecimal importe) {
        if (pagado.add(importe).compareTo(total) > 0) {
            throw new ImportePagoExcedidoException(facturaId, total.subtract(pagado));
        }
    }

    private static EstadoFactura estado(BigDecimal total, BigDecimal pagado) {
        if (pagado.compareTo(total) >= 0) {
            return EstadoFactura.PAGADA;
        }
        if (pagado.signum() == 0) {
            return EstadoFactura.PENDIENTE;
        }
        return EstadoFactura.PARCIAL;
    }
}
