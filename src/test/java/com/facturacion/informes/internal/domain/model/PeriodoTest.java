package com.facturacion.informes.internal.domain.model;

import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 3, 15);

    @Test
    void aceptaUnPeriodoDeUnSoloDia() {
        Periodo periodo = new Periodo(DIA, DIA);

        assertThat(periodo.desde()).isEqualTo(DIA);
        assertThat(periodo.hasta()).isEqualTo(DIA);
    }

    @Test
    void aceptaUnPeriodoQueCruzaElAnio() {
        Periodo periodo = new Periodo(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1));

        assertThat(periodo.desde()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(periodo.hasta()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void rechazaDesdePosteriorAHasta() {
        assertThatThrownBy(() -> new Periodo(DIA.plusDays(1), DIA))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("La fecha desde del informe no puede ser posterior a la fecha hasta");
    }

    @Test
    void rechazaFechasAusentes() {
        assertThatThrownBy(() -> new Periodo(null, DIA))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("Las fechas desde y hasta del informe son obligatorias");
        assertThatThrownBy(() -> new Periodo(DIA, null))
                .isInstanceOf(InformeInvalidoException.class)
                .hasMessage("Las fechas desde y hasta del informe son obligatorias");
    }
}
