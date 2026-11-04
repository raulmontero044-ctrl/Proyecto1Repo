package com.facturacion.factura.internal.domain.model;

import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;

import java.math.BigDecimal;
public record LineaFactura(String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {

    public LineaFactura {
        if (concepto == null || concepto.isBlank()) {
            throw new FacturaInvalidaException("El concepto de la línea es obligatorio");
        }
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new FacturaInvalidaException("La cantidad de la línea debe ser mayor que cero");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new FacturaInvalidaException("El precio unitario de la línea no puede ser negativo");
        }
    }
}
