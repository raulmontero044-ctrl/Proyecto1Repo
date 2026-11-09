package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

import java.time.LocalDate;

public record Periodo(LocalDate desde, LocalDate hasta) {

    public Periodo {
        if (desde == null || hasta == null) {
            throw new InformeInvalidoException("Las fechas desde y hasta del informe son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new InformeInvalidoException("La fecha desde del informe no puede ser posterior a la fecha hasta");
        }
    }
}
