package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.TipoFactura;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class CalculadoraInformeFacturas {

    private static final int ESCALA = 2;
    private static final Comparator<FacturaResumida> POR_FECHA_E_ID = Comparator
            .comparing(FacturaResumida::fecha)
            .thenComparing(FacturaResumida::id, Comparator.nullsLast(Comparator.naturalOrder()));

    private CalculadoraInformeFacturas() {
    }

    public static ResumenFacturas resumen(List<FacturaResumida> facturas, TipoFactura tipo) {
        List<FacturaResumida> delTipo = delTipo(facturas, tipo).stream().sorted(POR_FECHA_E_ID).toList();
        return new ResumenFacturas(
                delTipo,
                delTipo.size(),
                sumar(delTipo, FacturaResumida::base),
                sumar(delTipo, FacturaResumida::cuotaIva),
                sumar(delTipo, FacturaResumida::total));
    }

    public static IngresosGastos ingresosGastos(List<FacturaResumida> facturas) {
        BigDecimal ingresos = sumar(delTipo(facturas, TipoFactura.EMITIDA), FacturaResumida::base);
        BigDecimal gastos = sumar(delTipo(facturas, TipoFactura.RECIBIDA), FacturaResumida::base);
        return new IngresosGastos(ingresos, gastos, ingresos.subtract(gastos));
    }

    private static List<FacturaResumida> delTipo(List<FacturaResumida> facturas, TipoFactura tipo) {
        return facturas.stream().filter(factura -> factura.tipo() == tipo).toList();
    }

    private static BigDecimal sumar(List<FacturaResumida> facturas, Function<FacturaResumida, BigDecimal> importe) {
        return facturas.stream()
                .map(importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }
}
