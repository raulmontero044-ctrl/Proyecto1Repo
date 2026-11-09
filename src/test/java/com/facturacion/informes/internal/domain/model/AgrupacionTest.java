package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgrupacionTest {

    @ParameterizedTest
    @CsvSource({
            "2026-01-01, 2026-01",
            "2026-03-31, 2026-03",
            "2026-12-15, 2026-12"})
    void laClaveMensualEsAnioYMesConDosCifras(LocalDate fecha, String clave) {
        assertThat(Agrupacion.MES.clave(fecha)).isEqualTo(clave);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01, 2026-T1",
            "2026-03-31, 2026-T1",
            "2026-04-01, 2026-T2",
            "2026-06-30, 2026-T2",
            "2026-07-01, 2026-T3",
            "2026-09-30, 2026-T3",
            "2026-10-01, 2026-T4",
            "2026-12-31, 2026-T4"})
    void laClaveTrimestralAgrupaCadaTresMeses(LocalDate fecha, String clave) {
        assertThat(Agrupacion.TRIMESTRE.clave(fecha)).isEqualTo(clave);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01, 2026",
            "2026-12-31, 2026",
            "2027-01-01, 2027"})
    void laClaveAnualEsElAnio(LocalDate fecha, String clave) {
        assertThat(Agrupacion.ANIO.clave(fecha)).isEqualTo(clave);
    }

    @ParameterizedTest
    @CsvSource({
            "MES, 2026-05-20, 2026-05-01",
            "TRIMESTRE, 2026-02-14, 2026-01-01",
            "TRIMESTRE, 2026-05-20, 2026-04-01",
            "TRIMESTRE, 2026-08-31, 2026-07-01",
            "TRIMESTRE, 2026-12-31, 2026-10-01",
            "ANIO, 2026-05-20, 2026-01-01"})
    void elInicioEsElPrimerDiaDelPeriodo(Agrupacion agrupacion, LocalDate fecha, LocalDate inicio) {
        assertThat(agrupacion.inicio(fecha)).isEqualTo(inicio);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MES", "TRIMESTRE", "ANIO"})
    void seObtieneDesdeSuNombre(String valor) {
        assertThat(Agrupacion.desde(valor)).isEqualTo(Agrupacion.valueOf(valor));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "mes", "SEMANA", "AÑO"})
    void rechazaUnaAgrupacionDesconocida(String valor) {
        assertThatThrownBy(() -> Agrupacion.desde(valor))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La agrupación del informe debe ser MES, TRIMESTRE o ANIO");
    }

    @Test
    void rechazaUnaAgrupacionAusente() {
        assertThatThrownBy(() -> Agrupacion.desde(null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La agrupación del informe es obligatoria");
    }
}
