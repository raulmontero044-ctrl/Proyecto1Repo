package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.PagoResumido;
import com.facturacion.informes.internal.domain.model.ResumenPendientes;
import com.facturacion.informes.internal.domain.model.ResumenTesoreria;
import com.facturacion.informes.internal.domain.model.SaldoFactura;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TipoPago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

public final class CalculadoraInformePagos {

    private static final int ESCALA = 2;

    private CalculadoraInformePagos() {
    }

    public static ResumenPendientes pendientes(List<SaldoFactura> saldos) {
        List<SaldoFactura> noPagadas = saldos.stream()
                .filter(saldo -> saldo.estado().tienePendiente())
                .toList();
        List<SaldoFactura> emitidas = delTipo(noPagadas, TipoFactura.EMITIDA);
        List<SaldoFactura> recibidas = delTipo(noPagadas, TipoFactura.RECIBIDA);
        return new ResumenPendientes(
                pendiente(emitidas.stream()),
                pendiente(emitidas.stream().filter(SaldoFactura::vencida)),
                pendiente(recibidas.stream()),
                pendiente(recibidas.stream().filter(SaldoFactura::vencida)),
                noPagadas);
    }

    public static ResumenTesoreria tesoreria(List<PagoResumido> pagos) {
        BigDecimal cobros = importe(pagos, TipoPago.COBRO);
        BigDecimal pagados = importe(pagos, TipoPago.PAGO);
        return new ResumenTesoreria(cobros, pagados, cobros.subtract(pagados));
    }

    private static List<SaldoFactura> delTipo(List<SaldoFactura> saldos, TipoFactura tipo) {
        return saldos.stream().filter(saldo -> saldo.tipo() == tipo).toList();
    }

    private static BigDecimal pendiente(Stream<SaldoFactura> saldos) {
        return sumar(saldos.map(SaldoFactura::pendiente));
    }

    private static BigDecimal importe(List<PagoResumido> pagos, TipoPago tipo) {
        return sumar(pagos.stream().filter(pago -> pago.tipo() == tipo).map(PagoResumido::importe));
    }

    private static BigDecimal sumar(Stream<BigDecimal> importes) {
        return importes.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(ESCALA, RoundingMode.HALF_UP);
    }
}
