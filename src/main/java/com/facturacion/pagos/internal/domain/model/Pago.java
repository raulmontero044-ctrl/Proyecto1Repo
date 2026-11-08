package com.facturacion.pagos.internal.domain.model;

import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record Pago(
        Long id,
        Long facturaId,
        Long empresaId,
        TipoPago tipo,
        LocalDate fecha,
        BigDecimal importe,
        MetodoPago metodo,
        String referencia) {

    private static final int LONGITUD_MAXIMA_REFERENCIA = 100;

    public Pago {
        if (facturaId == null) {
            throw new PagoInvalidoException("La factura del pago es obligatoria");
        }
        if (empresaId == null) {
            throw new PagoInvalidoException("La empresa del pago es obligatoria");
        }
        if (tipo == null) {
            throw new PagoInvalidoException("El tipo del pago es obligatorio");
        }
        if (fecha == null) {
            throw new PagoInvalidoException("La fecha del pago es obligatoria");
        }
        importe = importeConDosDecimales(importe);
        if (metodo == null) {
            throw new PagoInvalidoException("El método del pago es obligatorio");
        }
        if (referencia != null && referencia.length() > LONGITUD_MAXIMA_REFERENCIA) {
            throw new PagoInvalidoException(
                    "La referencia del pago no puede superar " + LONGITUD_MAXIMA_REFERENCIA + " caracteres");
        }
    }

    public static Pago nuevo(
            Long facturaId,
            Long empresaId,
            TipoPago tipo,
            LocalDate fecha,
            BigDecimal importe,
            MetodoPago metodo,
            String referencia,
            LocalDate hoy) {
        Pago pago = new Pago(null, facturaId, empresaId, tipo, fecha, importe, metodo, referencia);
        if (pago.fecha().isAfter(hoy)) {
            throw new PagoInvalidoException("La fecha del pago no puede ser posterior a hoy");
        }
        return pago;
    }

    private static BigDecimal importeConDosDecimales(BigDecimal importe) {
        if (importe == null || importe.signum() <= 0) {
            throw new PagoInvalidoException("El importe del pago debe ser mayor que cero");
        }
        try {
            return importe.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException excepcion) {
            throw new PagoInvalidoException("El importe del pago no puede tener más de dos decimales");
        }
    }
}
