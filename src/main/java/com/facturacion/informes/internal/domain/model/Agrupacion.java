package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;

import java.time.LocalDate;
import java.time.YearMonth;

public enum Agrupacion {
    MES,
    TRIMESTRE,
    ANIO;

    public static Agrupacion desde(String valor) {
        if (valor == null) {
            throw new InformeInvalidoException("La agrupación del informe es obligatoria");
        }
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException excepcion) {
            throw new InformeInvalidoException("La agrupación del informe debe ser MES, TRIMESTRE o ANIO");
        }
    }

    public LocalDate inicio(LocalDate fecha) {
        return switch (this) {
            case MES -> fecha.withDayOfMonth(1);
            case TRIMESTRE -> LocalDate.of(fecha.getYear(), primerMesDelTrimestre(fecha), 1);
            case ANIO -> fecha.withDayOfYear(1);
        };
    }

    public String clave(LocalDate fecha) {
        return switch (this) {
            case MES -> YearMonth.from(fecha).toString();
            case TRIMESTRE -> fecha.getYear() + "-T" + trimestre(fecha);
            case ANIO -> String.valueOf(fecha.getYear());
        };
    }

    private static int trimestre(LocalDate fecha) {
        return (fecha.getMonthValue() - 1) / 3 + 1;
    }

    private static int primerMesDelTrimestre(LocalDate fecha) {
        return (trimestre(fecha) - 1) * 3 + 1;
    }
}
