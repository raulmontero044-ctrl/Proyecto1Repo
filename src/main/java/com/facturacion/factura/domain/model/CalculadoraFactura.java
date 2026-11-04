package com.facturacion.factura.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CalculadoraFactura {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraFactura() {
    }

    public static BigDecimal importe(LineaFactura linea) {
        return linea.cantidad().multiply(linea.precioUnitario()).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal base(Factura factura) {
        return factura.lineas().stream()
                .map(CalculadoraFactura::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal cuotaIva(Factura factura) {
        return base(factura).multiply(factura.porcentajeIva()).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal total(Factura factura) {
        return base(factura).add(cuotaIva(factura)).setScale(2, RoundingMode.HALF_UP);
    }
}
