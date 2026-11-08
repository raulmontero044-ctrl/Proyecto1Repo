package com.facturacion.pagos.internal.domain.exception;

public class PagoNoEncontradoException extends RuntimeException {

    public PagoNoEncontradoException(Long id) {
        super("No existe el pago con id " + id);
    }
}
