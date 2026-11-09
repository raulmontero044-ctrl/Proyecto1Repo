package com.facturacion.informes.internal.domain.service;

import com.facturacion.informes.internal.domain.model.Agrupacion;
import com.facturacion.informes.internal.domain.model.EstadisticaPeriodo;
import com.facturacion.informes.internal.domain.model.FacturaResumida;
import com.facturacion.informes.internal.domain.model.IngresosGastos;
import com.facturacion.informes.internal.domain.model.ResumenFacturas;
import com.facturacion.informes.internal.domain.model.ResumenImpuestos;
import com.facturacion.informes.internal.domain.model.TipoFactura;
import com.facturacion.informes.internal.domain.model.TramoIva;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static ResumenImpuestos impuestos(List<FacturaResumida> facturas) {
        List<TramoIva> repercutido = tramos(delTipo(facturas, TipoFactura.EMITIDA));
        List<TramoIva> soportado = tramos(delTipo(facturas, TipoFactura.RECIBIDA));
        BigDecimal cuotaRepercutida = sumarCuotas(repercutido);
        BigDecimal cuotaSoportada = sumarCuotas(soportado);
        return new ResumenImpuestos(
                repercutido, cuotaRepercutida, soportado, cuotaSoportada, cuotaRepercutida.subtract(cuotaSoportada));
    }

    public static List<EstadisticaPeriodo> estadisticas(List<FacturaResumida> facturas, Agrupacion agrupacion) {
        Map<LocalDate, List<FacturaResumida>> porPeriodo = facturas.stream()
                .collect(Collectors.groupingBy(
                        factura -> agrupacion.inicio(factura.fecha()), TreeMap::new, Collectors.toList()));
        return porPeriodo.entrySet().stream()
                .map(periodo -> estadistica(agrupacion.clave(periodo.getKey()), periodo.getValue()))
                .toList();
    }

    private static EstadisticaPeriodo estadistica(String periodo, List<FacturaResumida> facturas) {
        List<FacturaResumida> emitidas = delTipo(facturas, TipoFactura.EMITIDA);
        List<FacturaResumida> recibidas = delTipo(facturas, TipoFactura.RECIBIDA);
        return new EstadisticaPeriodo(
                periodo,
                emitidas.size(),
                sumar(emitidas, FacturaResumida::total),
                recibidas.size(),
                sumar(recibidas, FacturaResumida::total));
    }

    private static List<TramoIva> tramos(List<FacturaResumida> facturas) {
        Map<BigDecimal, List<FacturaResumida>> porPorcentaje = facturas.stream()
                .collect(Collectors.groupingBy(
                        factura -> factura.porcentajeIva().setScale(ESCALA, RoundingMode.HALF_UP),
                        TreeMap::new,
                        Collectors.toList()));
        return porPorcentaje.entrySet().stream()
                .map(tramo -> new TramoIva(
                        tramo.getKey(),
                        sumar(tramo.getValue(), FacturaResumida::base),
                        sumar(tramo.getValue(), FacturaResumida::cuotaIva)))
                .toList();
    }

    private static BigDecimal sumarCuotas(List<TramoIva> tramos) {
        return tramos.stream()
                .map(TramoIva::cuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, RoundingMode.HALF_UP);
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
