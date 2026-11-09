package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

import java.math.BigDecimal;

public record PagoResumido(Long id, TipoPago tipo, BigDecimal importe) {

    public PagoResumido {
        if (tipo == null) {
            throw new InformeInvalidoException("El tipo del pago es obligatorio");
        }
        if (importe == null) {
            throw new InformeInvalidoException("El importe del pago es obligatorio");
        }
    }
}
